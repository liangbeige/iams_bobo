package com.iams.manage.domain.OcrUploadDTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class OcrUpload {
    private String file;
    private Long archiveId;
    private String name;
    private String content;
}
