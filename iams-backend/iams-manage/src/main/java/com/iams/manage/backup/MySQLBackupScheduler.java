package com.iams.manage.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

@Component
public class MySQLBackupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MySQLBackupScheduler.class);

    @Value("${spring.datasource.druid.master.username}")
    private String username;

    @Value("${spring.datasource.druid.master.password}")
    private String password;

    @Value("${backup.storage-path}")
    private String backupPath;

    @Scheduled(cron = "0 1 0 7 * ?")
    public void backup() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
            String fileName = "backup_" + timeStamp + ".sql";
            Path backupDir = Paths.get(backupPath);

            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            // 使用 ProcessBuilder 执行 mysqldump
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("D:\\Java\\MySQL\\MySQL Server 8.0\\bin\\mysqldump", "-u" + username, "-p" + password, "--databases", "iams0319based0125");
            processBuilder.redirectOutput(backupDir.resolve(fileName).toFile());
            processBuilder.redirectErrorStream(true); // 将错误流重定向到输出流

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("备份成功: {}", backupDir.resolve(fileName));
            } else {
                logger.error("备份失败，退出代码: {}", exitCode);
            }

        } catch (IOException | InterruptedException e) {
            logger.error("备份过程中发生异常", e);
        }
    }
}