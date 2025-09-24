package com.iams.elasticsearch.service.impl;


import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;

@Service
public class DocumentContentExtractor {

    /**
     * 从文件中提取文本内容
     * @param file 文件
     * @param fileType 文件类型
     * @return 提取的文本内容
     */
    public String extractContent(File file, String fileType) throws Exception {
        switch (fileType.toLowerCase()) {
            case "doc":
            case "docx":
                return extractWordContent(file);
            case "txt":
                return extractTxtContent(file);
            default:
                throw new UnsupportedOperationException("Unsupported file type: " + fileType);
        }
    }



    private String extractWordContent(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractTxtContent(File file) throws Exception {
        return Files.readString(file.toPath());
    }
}