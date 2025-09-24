package com.iams.manage.backup;


//CESHI GIT
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.snapshot.CreateSnapshotRequest;
import co.elastic.clients.elasticsearch.snapshot.CreateSnapshotResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

@Component
public class ElasticsearchBackupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchBackupScheduler.class);

    // elasticsearch备份
    @Scheduled(cron = "0 0 2 7 * ?")
    public void backup() {
        createSnapshot();
    }

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private static final String REPOSITORY_NAME = "iams_backup_repo";

    public void createSnapshot() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
            String snapshotName = "backup_" + timeStamp;

            CreateSnapshotRequest request = CreateSnapshotRequest.of(b->b
                    .repository(REPOSITORY_NAME)
                    .snapshot(snapshotName)
                    .waitForCompletion(true));

            CreateSnapshotResponse response = elasticsearchClient.snapshot().create(request);

            logger.info("创建快照成功: {}", response.snapshot());

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}
