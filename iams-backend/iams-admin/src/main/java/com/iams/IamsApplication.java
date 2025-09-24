package com.iams;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动程序
 * 
 * @author zhjm
 */
@EnableFileStorage
@EnableScheduling   // 启用定时任务
@EnableAsync     //启用异步任务
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class IamsApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(IamsApplication.class, args);
        System.out.println("智慧档案管理系统启动成功！\n");
    }
}
