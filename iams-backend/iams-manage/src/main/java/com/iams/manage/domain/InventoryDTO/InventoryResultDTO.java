package com.iams.manage.domain.InventoryDTO;


import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class InventoryResultDTO {
    private Date inventoryTime;
    private Integer totalCount;
    private Integer scannedCount;
    private List<InventoryRecord> records;

    @Data
    public static class InventoryRecord {
        private Long archiveId;
        private String rfid;
        private String danghao;
        private Date scanTime;
        private String antenna;
        private String rssi;
        private String shitiLocation;
        private String exactLocation;
        private Boolean manual;
    }
}
