package com.iams.manage.domain.MergeRequestDTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class MergeRequest {
    private String fileHash;
    private String filename;
    private int totalChunks;
    private Long archiveId;
    private Long directory;
    private boolean isEncrypted;
    private String iv;
    private String encryptionKey;
    private boolean encrypted;
}
