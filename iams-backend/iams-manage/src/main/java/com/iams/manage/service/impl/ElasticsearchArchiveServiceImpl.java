package com.iams.manage.service.impl;



import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.ScriptSortType;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.InnerHitsResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.iams.common.utils.SearchContextHolder;
import com.iams.common.utils.SecurityUtils;
import com.iams.elasticsearch.domain.ElasticsearchArchive;
import com.iams.manage.mapper.UserPostMapper;
import com.iams.manage.service.IElasticsearchArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

//import static com.iams.framework.datasource.DynamicDataSourceContextHolder.log;

@Service
public class ElasticsearchArchiveServiceImpl implements IElasticsearchArchiveService {

    private final ElasticsearchClient client;

    private static final Map<String, List<String>> POST_SECURITY_RULES = Map.of(
            "user", List.of("0", "1", "2", "3", "4"),
            "auditor", List.of("0", "1", "2", "3", "4"),
            "director", List.of("0", "1", "2", "3", "4")
    );

    @Autowired
    public ElasticsearchArchiveServiceImpl(ElasticsearchClient client) {
        this.client = client;
    }

    @Autowired
    public UserPostMapper userPostMapper;

    @Override
    public List<ElasticsearchArchive> CombineSearch(Map<String, String> combineParams) {
        List<ElasticsearchArchive> list = new ArrayList<>();
//        int restrictedCount = 0;

        // 1. 创建查询Builder
        BoolQuery.Builder queryBuilder = new BoolQuery.Builder();

        // 收集所有查询字段，用于高亮
        Set<String> queryFields = new HashSet<>();

        // 2. 构建基础查询条件
        buildBaseQueryConditions(queryBuilder, combineParams, queryFields);

        int pageNum = Integer.parseInt(combineParams.getOrDefault("pageNum", "1"));
        int pageSize = Integer.parseInt(combineParams.getOrDefault("pageSize", "15"));

        try {
            // 3. 执行搜索查询（不带权限过滤）
            SearchResponse<ElasticsearchArchive> response = executeDataQueryWithPagination(queryBuilder, queryFields, pageNum, pageSize);
//                    executeDataQuery(queryBuilder, queryFields);

//            // 4. 获取用户权限级别
//            List<String> allowedLevels = getAllowedSecurityLevels(getCurrentUserPostCodes());

            // 5. 处理搜索结果并进行权限过滤
            List<ElasticsearchArchive> allResults = new ArrayList<>();
            processSearchResults(response, allResults);

//            // 6. 对结果进行权限筛选
//            for (ElasticsearchArchive archive : allResults) {
//                if (hasPermissionToAccess(archive.getSecretLevel(), allowedLevels)) {
//                    list.add(archive);
//                } else {
//                    restrictedCount++;
//                }
//            }

            // 直接添加所有结果
            list.addAll(allResults);

//            // 7. 保存无权限记录数
//            SearchContextHolder.setRestrictedCount(restrictedCount);

        } catch (IOException e) {
            //log.error("执行 Elasticsearch 查询失败", e);
            throw new RuntimeException("执行 Elasticsearch 查询失败", e);
        }

        return list;
    }

    // 嵌套条件数据类
    private static class NestedCondition {
        String field;   // 如 "documents.docTitle"
        String value;   // 如 "身份"
        String logic;   // 如 "AND"

        NestedCondition(String field, String value, String logic) {
            this.field = field;
            this.value = value;
            this.logic = logic;
        }
    }

    // 普通条件数据类
    private static class RegularCondition {
        String field;   // 如 "title"
        String value;   // 如 "717"
        String logic;   // 如 "AND"

        RegularCondition(String field, String value, String logic) {
            this.field = field;
            this.value = value;
            this.logic = logic;
        }
    }

    // 普通条件处理方法
    private void addRegularCondition(BoolQuery.Builder builder, String logic, String field, String value) {
        if (field == null || value == null || field.isEmpty() || value.isEmpty()) return;

        final Query query;
        if (field.equals("mysqlDanghao")) {
            query = Query.of(q -> q.term(t -> t.field(field).value(value)));
        } else {
            query = Query.of(q -> q.match(m -> m.field(field).query(value)));
        }

        switch (logic.toUpperCase()) {
            case "OR":
                builder.should(query);
                break;
            case "NOT":
                builder.mustNot(query);
                break;
            case "AND":
            default:
                builder.must(query);
                break;
        }
    }



    /**
     * 构建基础查询条件（公共部分）
     */
    private void buildBaseQueryConditions(BoolQuery.Builder builder,
                                          Map<String, String> params,
                                          Set<String> queryFields) {

        // 收集所有嵌套字段条件和普通字段条件
        List<NestedCondition> nestedConditions = new ArrayList<>();
        List<RegularCondition> regularConditions = new ArrayList<>();

        // 处理主条件1
        if (params.containsKey("MainType") && params.containsKey("MainTypeValue")) {
            String field = params.get("MainType");
            String value = params.get("MainTypeValue");
            String logic = params.getOrDefault("MainTypeLogic", "AND");

            if (field.startsWith("documents.")) {
                // 嵌套字段 - 加入嵌套条件列表
                nestedConditions.add(new NestedCondition(field, value, logic));
            } else {
                // 普通字段 - 加入普通条件列表
                regularConditions.add(new RegularCondition(field, value, logic));
            }
            queryFields.add(field);
        }

        // 处理主条件2
        if (params.containsKey("secondMainType") && params.containsKey("secondMainTypeValue")) {
            String field = params.get("secondMainType");
            String value = params.get("secondMainTypeValue");
            String logic = params.getOrDefault("secondMainTypeLogic", "AND");

            if (field.startsWith("documents.")) {
                // 再次遇到嵌套字段 - 继续加入同一个嵌套条件列表
                nestedConditions.add(new NestedCondition(field, value, logic));
            } else {
                regularConditions.add(new RegularCondition(field, value, logic));
            }
            queryFields.add(field);
        }

        // 处理动态条件（如果有的话）
        for (int i = 1; i <= 10; i++) {
            String dynamicField = params.get("dynamicField" + i);
            String dynamicValue = params.get("dynamicValue" + i);
            String dynamicLogic = params.getOrDefault("dynamicLogic" + i, "AND");

            if (dynamicField != null && dynamicValue != null && !dynamicField.isEmpty() && !dynamicValue.isEmpty()) {
                if (dynamicField.startsWith("documents.")) {
                    nestedConditions.add(new NestedCondition(dynamicField, dynamicValue, dynamicLogic));
                } else {
                    regularConditions.add(new RegularCondition(dynamicField, dynamicValue, dynamicLogic));
                }
                queryFields.add(dynamicField);
            }
        }

        // 关键：统一处理所有嵌套条件（在同一个nested查询中）
        if (!nestedConditions.isEmpty()) {
            addNestedConditions(builder, nestedConditions);
        }

        // 处理普通条件
        for (RegularCondition condition : regularConditions) {
            addRegularCondition(builder, condition.logic, condition.field, condition.value);
        }
    }


    private void addNestedConditions(BoolQuery.Builder builder, List<NestedCondition> conditions) {
        // 创建嵌套查询内部的bool查询构建器
        BoolQuery.Builder nestedBoolBuilder = new BoolQuery.Builder();

        // 遍历所有嵌套条件，添加到同一个bool查询中
        for (NestedCondition condition : conditions) {
            // 去掉 "documents." 前缀，获取实际字段名
            String nestedField = condition.field.substring("documents.".length());

            // 为每个条件创建match查询
            Query matchQuery = Query.of(q -> q.match(m -> m
                    .field("documents." + nestedField)  // 完整字段路径
                    .query(condition.value)
            ));

            // 根据逻辑操作符添加到bool查询中
            switch (condition.logic.toUpperCase()) {
                case "OR":
                    nestedBoolBuilder.should(matchQuery);
                    break;
                case "NOT":
                    nestedBoolBuilder.mustNot(matchQuery);
                    break;
                case "AND":
                default:
                    // 关键：用must实现交集
                    nestedBoolBuilder.must(matchQuery);
                    break;
            }
        }

        // 创建单个nested查询，包含所有条件的组合
        Query nestedQuery = Query.of(q -> q
                .nested(n -> n
                        .path("documents")  // 指定嵌套路径
                        .query(nestedBoolBuilder.build()._toQuery())  // 内部查询
                        .innerHits(ih -> ih  // 用于高亮显示
                                .size(100)
                                .highlight(h -> h
                                        .fields("documents.docTitle", HighlightField.of(hf -> hf
                                                .preTags("<span class='highlight'>")
                                                .postTags("</span>")
                                        ))
                                        .fields("documents.docContent", HighlightField.of(hf -> hf
                                                .preTags("<span class='highlight'>")
                                                .postTags("</span>")
                                        ))
                                )
                        )
                ));

        // 将整个嵌套查询添加到主查询中
        builder.must(nestedQuery);
    }

    /**
     * 执行数据查询（带高亮和分页）
     * 新增方法：支持分页查询
     */
    private SearchResponse<ElasticsearchArchive> executeDataQueryWithPagination(
            BoolQuery.Builder builder,
            Set<String> queryFields,
            int pageNum,
            int pageSize) throws IOException {

        // 计算ES的from参数（跳过的文档数）
        int from = (pageNum - 1) * pageSize;

        // 构建高亮配置
        Map<String, HighlightField> highlightFields = buildHighlightFields(queryFields);

        return client.search(s -> s
                        .index("archives")
                        .query(q -> q.bool(builder.build()))
                        .from(from)  // 设置起始位置
                        .size(pageSize)  // 设置每页大小
                //基础排序
//                        .sort(sort -> sort
//                                .field(f -> f
//                                        .field("mysqlDanghao") // 先试试不带.keyword
//                                        .order(SortOrder.Asc)
//                                        .missing("_last")
//                                )
//                        )
//                        .sort(sort -> sort.score(sc -> sc.order(SortOrder.Desc)))


                        .highlight(h -> h
                                .fields(highlightFields)
                                .requireFieldMatch(false)
                        ),
                ElasticsearchArchive.class);
    }

    /**
     * 获取用户允许访问的密级列表
     */
//    private List<String> getAllowedSecurityLevels(List<String> userPosts) {
//        if (userPosts == null || userPosts.isEmpty()) {
//            return List.of("0"); // 默认至少可以访问公开级别
//        }
//
//        return userPosts.stream()
//                .flatMap(post -> POST_SECURITY_RULES.getOrDefault(post, List.of("0")).stream())
//                .distinct()
//                .collect(Collectors.toList());
//    }

    /**
     * 检查是否有权限访问指定密级的档案
     */
    private boolean hasPermissionToAccess(String archiveSecretLevel, List<String> allowedLevels) {
        if (archiveSecretLevel == null) {
            return true; // 如果档案没有密级信息，默认允许访问
        }
        return allowedLevels.contains(archiveSecretLevel);
    }

    /**
     * 执行数据查询（带高亮）
     */
    private SearchResponse<ElasticsearchArchive> executeDataQuery(
            BoolQuery.Builder builder,
            Set<String> queryFields) throws IOException {

        // 构建高亮配置
        Map<String, HighlightField> highlightFields = buildHighlightFields(queryFields);

        return client.search(s -> s
                        .index("archives")
                        .query(q -> q.bool(builder.build()))
                        .highlight(h -> h
                                .fields(highlightFields)
                                .requireFieldMatch(false)
                        ),
                ElasticsearchArchive.class);
    }

    /**
     * 构建高亮字段配置
     */
    private Map<String, HighlightField> buildHighlightFields(Set<String> queryFields) {
        Map<String, HighlightField> highlightFields = new HashMap<>();
        for (String field : queryFields) {
            highlightFields.put(field, HighlightField.of(hf -> hf
                    .preTags("<span class='highlight'>")
                    .postTags("</span>")
            ));
            // 处理嵌套字段
            if (field.startsWith("documents.")) {
                String nestedField = field.substring("documents.".length());
                // 为嵌套字段添加高亮，使用完整的路径
                highlightFields.put("documents." + nestedField, HighlightField.of(hf -> hf
                        .preTags("<span class='highlight'>")
                        .postTags("</span>")
                ));
            }
        }

        // 确保常用的嵌套字段都被包含
        highlightFields.put("documents.docTitle", HighlightField.of(hf -> hf
                .preTags("<span class='highlight'>")
                .postTags("</span>")
        ));
        highlightFields.put("documents.docContent", HighlightField.of(hf -> hf
                .preTags("<span class='highlight'>")
                .postTags("</span>")
        ));


        return highlightFields;
    }

    public long getTotalCount(Map<String, String> combineParams) {
        BoolQuery.Builder queryBuilder = new BoolQuery.Builder();
        Set<String> queryFields = new HashSet<>();

        // 使用相同的查询条件构建逻辑，但不包含分页参数
        Map<String, String> countParams = new HashMap<>(combineParams);
        countParams.remove("pageNum");  // 移除分页参数
        countParams.remove("pageSize"); // 移除分页参数

        // 使用相同的查询条件构建逻辑
        buildBaseQueryConditions(queryBuilder, combineParams, queryFields);

        try {
            var countResponse = client.count(c -> c
                    .index("archives")
                    .query(q -> q.bool(queryBuilder.build()))
            );
            return countResponse.count();
        } catch (IOException e) {
            throw new RuntimeException("获取总数失败", e);
        }
    }

    /**
     * 处理搜索结果
     */
//    private void processSearchResults(SearchResponse<ElasticsearchArchive> response,
//                                      List<ElasticsearchArchive> resultList) {
//        for (Hit<ElasticsearchArchive> hit : response.hits().hits()) {
//            if (hit.source() != null) {
//                ElasticsearchArchive archive = hit.source();
//                // 设置高亮结果
//                if (hit.highlight() != null) {
//                    Map<String, List<String>> highlights = new HashMap<>();
//                    for (Map.Entry<String, List<String>> entry : hit.highlight().entrySet()) {
//                        String key = entry.getKey();
//
//                        List<String> highlightValues = entry.getValue();
//
//                        // 直接存储高亮信息，保持原始的key格式
//                        highlights.put(key, highlightValues);
//
//
//                        if (key.startsWith("documents.")) {
//                            String simplifiedKey = key.substring("documents.".length());
//                            highlights.put(simplifiedKey, highlightValues);
//                        }
//                    }
//                    archive.setHighlights(highlights);
//
//                    // 添加调试日志
//                    System.out.println("Archive ID: " + archive.getId() + ", Highlights: " + highlights.keySet());
//
//                }
//                resultList.add(archive);
//            }
//        }
//    }
    private void processSearchResults(SearchResponse<ElasticsearchArchive> response,
                                      List<ElasticsearchArchive> resultList) {
        for (Hit<ElasticsearchArchive> hit : response.hits().hits()) {
            if (hit.source() != null) {
                ElasticsearchArchive archive = hit.source();

                // 设置高亮结果
                if (hit.highlight() != null) {
                    Map<String, List<String>> highlights = new HashMap<>();

                    // 处理常规字段的高亮
                    for (Map.Entry<String, List<String>> entry : hit.highlight().entrySet()) {
                        String key = entry.getKey();
                        List<String> highlightValues = entry.getValue();

                        // 直接存储高亮信息，保持原始的key格式
                        highlights.put(key, highlightValues);

                        // 为了兼容性，也添加简化版本
                        if (key.startsWith("documents.")) {
                            String simplifiedKey = key.substring("documents.".length());
                            highlights.put(simplifiedKey, highlightValues);
                        }
                    }

                    // 处理嵌套文档的 inner_hits 高亮
                    if (hit.innerHits() != null) {
                        for (Map.Entry<String, InnerHitsResult> innerHitEntry : hit.innerHits().entrySet()) {
                            InnerHitsResult innerHitsResult = innerHitEntry.getValue();

                            // 遍历每个匹配的嵌套文档
                            for (Hit<?> innerHit : innerHitsResult.hits().hits()) {
                                if (innerHit.highlight() != null) {
                                    // 将嵌套文档的高亮信息合并到主文档的高亮中
                                    for (Map.Entry<String, List<String>> innerHighlight : innerHit.highlight().entrySet()) {
                                        String innerKey = innerHighlight.getKey();
                                        List<String> innerValues = innerHighlight.getValue();

                                        // 保持完整的嵌套字段路径
                                        highlights.put(innerKey, innerValues);

                                        // 同时添加简化版本
                                        if (innerKey.startsWith("documents.")) {
                                            String simplifiedInnerKey = innerKey.substring("documents.".length());
                                            highlights.put(simplifiedInnerKey, innerValues);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    archive.setHighlights(highlights);

                    // 添加调试日志
                    System.out.println("Archive ID: " + archive.getId() +
                            ", Total Highlights: " + highlights.size() +
                            ", Highlight Keys: " + highlights.keySet());
                }

                resultList.add(archive);
            }
        }
    }


//    private boolean isAdminUser(Long userId) {
//        return userId == 1L;
//    }
//
//    private List<String> getCurrentUserPostCodes() {
//        try {
//            Long userId = SecurityUtils.getLoginUser().getUserId();
//            if (isAdminUser(userId)) {
//                return List.of("director"); // 管理员默认为最高权限
//            }
//
//            Page<String> page = PageHelper.startPage(1, Integer.MAX_VALUE, false)
//                    .doSelectPage(() -> userPostMapper.selectPostCodesByUserId(userId));
//
//            //log.debug("用户{}岗位数据: {}", userId, page.getResult());
//            return page.getResult();
//
//        } catch (Exception e) {
//            //log.error("获取岗位失败", e);
//            return List.of("user"); // 默认为普通用户权限
//        }
//    }

    private void addCondition(BoolQuery.Builder builder, String logic, String field, String value) {
        if (field == null || value == null || field.isEmpty() || value.isEmpty()) return;

        final Query query;
        if (field.startsWith("documents.")) {
            String nestedField = field.substring("documents.".length());
            query = Query.of(q -> q
                    .nested(n -> n
                            .path("documents")
                            .query(Query.of(q2 -> q2
                                    .bool(b -> b
                                            .must(Query.of(q3 -> q3
                                                    .match(m -> m
                                                            .field("documents." + nestedField)
                                                            .query(value)
                                                    )
                                            ))
                                    )
                            ))
                            // 添加inner_hits来获取匹配的嵌套文档
                            .innerHits(ih -> ih
                                    .size(100)  // 设置返回的嵌套文档数量
                                    .highlight(h -> h
                                            .fields("documents." + nestedField,
                                                    HighlightField.of(hf -> hf
                                                            .preTags("<span class='highlight'>")
                                                            .postTags("</span>")
                                                    ))
                                    )
                            )
                    ));
        } else {
            if (field.equals("mysqlDanghao")) {
                query = Query.of(q -> q.term(t -> t.field(field).value(value)));
            } else {
                query = Query.of(q -> q.match(m -> m.field(field).query(value)));
            }
        }

        switch (logic.toUpperCase()) {
            case "OR":
                builder.should(query);
                break;
            case "NOT":
                builder.mustNot(query);
                break;
            case "AND":
            default:
                builder.must(query);
                break;
        }
    }
}