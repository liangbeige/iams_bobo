// SalesDataController.java
package com.ruoyi.web.controller;

import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ArchiveDataController {

    @GetMapping("/archive/classCount")
    public Map<String, Object> getArchiveClassInfo() {
        Random random = new Random();
        int seriesData1 = random.nextInt(1000);
        int seriesData2 = random.nextInt(1000);

        System.out.println("seriesData1: " + seriesData1 + ", seriesData2: " + seriesData2);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200); // 状态码
        response.put("msg", "请求成功"); // 消息
        response.put("data", new int[]{seriesData1, seriesData2}); // 实际数据
        return response;
    }

    @GetMapping("/browse/count")
    public Map<String, Object> getBrowseCountInfo(
            @RequestParam(value = "start_date", required = false) String startDateStr,
            @RequestParam(value = "end_date", required = false) String endDateStr) throws ParseException {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        System.out.println("startDate: " + startDateStr + ", endDate: " + endDateStr);

        // 设置默认时间区间
        Date startDate = dateFormat.parse("2024-05-13");
        Date endDate = dateFormat.parse("2024-05-16");


        // 如果提供了参数，则覆盖默认值
        if (startDateStr != null && endDateStr != null) {
            startDate = dateFormat.parse(startDateStr);
            endDate = dateFormat.parse(endDateStr);
        }

        // 生成 xAxisData
        List<String> xAxisData = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            xAxisData.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 生成 seriesData
        Random random = new Random();
        List<Integer> seriesData = new ArrayList<>();
        for (int i = 0; i < xAxisData.size(); i++) {
            seriesData.add(random.nextInt(1000)); // 随机生成 0 到 999 的数据
        }

        data.put("xAxisData", xAxisData);
        data.put("seriesData", seriesData);

        response.put("code", 200); // 状态码
        response.put("msg", "请求成功");
        response.put("data", data);

        return response;
    }

}