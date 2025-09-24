package com.iams.elasticsearch.repository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.iams.elasticsearch.domain.ElasticsearchArchive;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;





import java.util.List;
import java.util.Optional;

import static org.elasticsearch.search.aggregations.support.CoreValuesSourceType.log;

@Repository
public interface ArchiveRepository extends ElasticsearchRepository<ElasticsearchArchive, String> {

    // 根据标题或内容搜索
    List<ElasticsearchArchive> findByTitleContainingOrContentContaining(String title, String content);

    // 根据内容查询
    List<ElasticsearchArchive> findByContent(String content);

    // 根据文件类型和内容搜索
    List<ElasticsearchArchive> findByFileTypeAndContentContaining(String fileType, String content);

    // 根据保密级别和内容搜索
    List<ElasticsearchArchive> findBySecretLevelAndContentContaining(String secretLevel, String content);

    // 根据档号查询档案
    Optional<ElasticsearchArchive> findByMysqlDanghao(String mysqlDanghao);


//    // 关键：覆盖父接口方法，强制标记为修改操作
//    @Override
//    @Query(value = "{\"match_all\": {}}", modify = true) // 任意修改语句
    void deleteById(String id); // 将参数类型改为 String



    // 新增：通过mysqlDanghao删除（如果ES中mysqlDanghao是独立字段）
    void deleteByMysqlDanghao(String mysqlDanghao);



    @Query("""
    {
        "script": {
            "source": "ctx._source.documents.removeIf(doc -> doc.docId == params.docId)",
            "params": {
                "docId": "#{docId}"
            }
        }
    }
    """)
    void removeDocumentFromArchive(
            @Param("archiveId") String archiveId,
            @Param("docId") String docId
    );



}

