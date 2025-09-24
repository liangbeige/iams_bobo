package com.iams.service;

import com.iams.domain.Seal;
import com.iams.util.SealCircle;
import com.iams.util.SealConfiguration;
import com.iams.util.SealFont;
import com.iams.util.SealUtil;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public interface ISealService {
    // 印章生成相关方法
    byte[] generateOfficialSeal(String mainText, String viceText, String centerText) throws IOException;
    byte[] generatePersonalSeal(String name, String additionalText) throws IOException;

    // 本地保存相关方法
    void saveOfficialSealToLocal(String mainText, String viceText, String centerText, String filePath) throws IOException;
    void savePersonalSealToLocal(String name, String additionalText, String filePath) throws IOException;

    // 数据库操作相关方法
    List<Seal> selectSealList(Seal seal);
    Seal selectSealById(Long id);
    int insertSeal(Seal seal);
    int updateSeal(Seal seal);
    int deleteSealByIds(Long[] ids);
}