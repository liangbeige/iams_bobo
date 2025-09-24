package com.iams.quartz.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("backUpTask")
public class VastBaseBackUp {

    @Autowired
    private RestTemplate restTemplate;

    public void backup() {
        System.out.println("开始备份");

        try {
            String url = "http://192.168.1.11:9999/backup/logical-backup";

            // 调用远程接口
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("备份成功: " + response.getBody());
            } else {
                System.out.println("备份失败，状态码: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.out.println("备份调用异常: " + e.getMessage());
        }
    }

}
