package com.iams.manage.service;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Map;

import static com.iams.framework.datasource.DynamicDataSourceContextHolder.log;

@Service
public class ArchiveSyncService {
    private final ElasticsearchOperations elasticsearchOperations;
    private static final String DOCUMENT_TYPE = "_doc"; // ES 7.x+ 通用类型名

    public ArchiveSyncService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * 从档案中删除嵌套文档
     *
     * @param archiveDanghao 档案档号(ES中的ID)
     * @param docId          要删除的文档ID
     */
    public void removeNestedDocument(String archiveDanghao, String docId) {
        UpdateQuery updateQuery = UpdateQuery.builder(archiveDanghao)
                .withScript(buildRemoveScript(docId))
                .withParams(Map.of("docId", docId))  // 添加参数
//                .withScriptType(ScriptType.INLINE) // 使用枚举值 // 明确脚本类型
//                .withType(DOCUMENT_TYPE)  // 关键修复：添加type
                .build();

        // 通过反射强制设置type（解决Spring Data 5.3.6的BUG）
        setTypeViaReflection(updateQuery, DOCUMENT_TYPE);

        try {
            elasticsearchOperations.update(updateQuery, IndexCoordinates.of("archives"));
            log.info("ES删除成功 | 档号={} | 文档ID={}", archiveDanghao, docId);
        } catch (Exception e) {
            log.error("ES删除失败 | 档号={} | 错误: {}", archiveDanghao, e.getMessage());
            throw new RuntimeException("ES删除操作失败", e);
        }
    }

    private String buildRemoveScript(String docId) {
        return """
            if (ctx._source.documents != null) {
                ctx._source.documents.removeIf(doc -> doc.docId == params.docId);
            }
            """;
    }


    // 反射工具方法（兼容Spring Data Elasticsearch 5.3.6）
    private void setTypeViaReflection(UpdateQuery query, String type) {
        try {
            Field typeField = UpdateQuery.class.getDeclaredField("type");
            typeField.setAccessible(true);
            typeField.set(query, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set type via reflection", e);
        }
    }

}
