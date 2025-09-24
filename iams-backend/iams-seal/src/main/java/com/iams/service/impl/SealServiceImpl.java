package com.iams.service.impl;

import com.iams.domain.Seal;
import com.iams.mapper.SealMapper;
import com.iams.service.ISealService;
import com.iams.util.SealCircle;
import com.iams.util.SealConfiguration;
import com.iams.util.SealFont;
import com.iams.util.SealUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class SealServiceImpl implements ISealService {

    @Autowired
    private SealMapper SealMapper;

    @Override
    public byte[] generateOfficialSeal(String mainText, String viceText, String centerText) throws IOException {

        System.out.println("===== 开始生成印章 =====");
        System.out.println("主文字: " + mainText);

        SealConfiguration configuration = new SealConfiguration();

        // 主文字设置
        SealFont mainFont = new SealFont(mainText);
        mainFont.setBold(true);
        mainFont.setFontFamily("楷体");
        mainFont.setFontSize(25);
        mainFont.setFontSpace(12.0);
        mainFont.setMarginSize(10);
        configuration.setMainFont(mainFont);

        // 副文字设置
        if (viceText != null && !viceText.isEmpty()) {
            SealFont viceFont = new SealFont(viceText);
            viceFont.setBold(true);
            viceFont.setFontFamily("宋体");
            viceFont.setFontSize(22);
            viceFont.setFontSpace(12.0);
            viceFont.setMarginSize(5);
            configuration.setViceFont(viceFont);
        }

        // 中心文字设置
        if (centerText != null && !centerText.isEmpty()) {
            SealFont centerFont = new SealFont(centerText);
            centerFont.setBold(true);
            centerFont.setFontFamily("宋体");
            centerFont.setFontSize(25);
            configuration.setCenterFont(centerFont);
        }

        // 其他配置
        configuration.setImageSize(300);
        configuration.setBackgroudColor(Color.RED);
        configuration.setBorderCircle(new SealCircle(3, 140, 100));
        configuration.setBorderInnerCircle(new SealCircle(1, 135, 95));
        configuration.setInnerCircle(new SealCircle(2, 85, 45));

//        try {
//            return SealUtil.buildBytes(SealUtil.buildSeal(configuration));
//        } catch (Exception e) {
//            throw new IOException("生成公章失败", e);
//        }
        try {
            byte[] bytes = SealUtil.buildBytes(SealUtil.buildSeal(configuration));
            System.out.println("生成成功，数据大小: " + bytes.length + " bytes");
            return bytes;
        } catch (Exception e) {
            System.out.println("生成失败: " + e.getMessage());
            throw new IOException("生成公章失败", e);
        }
    }

    @Override
    public byte[] generatePersonalSeal(String name, String additionalText) throws IOException {
        SealFont font = new SealFont(name);
        font.setFontSize(120);
        font.setBold(true);
        font.setFontFamily("宋体");

        try {
            return SealUtil.buildBytes(SealUtil.buildPersonSeal(
                    300,
                    16,
                    font,
                    additionalText
            ));
        } catch (Exception e) {
            throw new IOException("生成私章失败", e);
        }
    }


    @Override
    public void saveOfficialSealToLocal(String mainText, String viceText, String centerText, String filePath) throws IOException {
        System.out.println("尝试保存到路径: " + new File(filePath).getAbsolutePath());

        // 使用与generateOfficialSeal相同的配置逻辑
        SealConfiguration configuration = new SealConfiguration();

        // 主文字设置
        SealFont mainFont = new SealFont(mainText);
        mainFont.setBold(true);
        mainFont.setFontFamily("楷体");
        mainFont.setFontSize(25);
        mainFont.setFontSpace(12.0);
        mainFont.setMarginSize(10);
        configuration.setMainFont(mainFont);

        // 副文字设置
        if (viceText != null && !viceText.isEmpty()) {
            SealFont viceFont = new SealFont(viceText);
            viceFont.setBold(true);
            viceFont.setFontFamily("宋体");
            viceFont.setFontSize(22);
            viceFont.setFontSpace(12.0);
            viceFont.setMarginSize(5);
            configuration.setViceFont(viceFont);
        }

        // 中心文字设置
        if (centerText != null && !centerText.isEmpty()) {
            SealFont centerFont = new SealFont(centerText);
            centerFont.setBold(true);
            centerFont.setFontFamily("宋体");
            centerFont.setFontSize(25);
            configuration.setCenterFont(centerFont);
        }

        // 其他配置
        configuration.setImageSize(300);
        configuration.setBackgroudColor(Color.RED);
        configuration.setBorderCircle(new SealCircle(3, 140, 100));
        configuration.setBorderInnerCircle(new SealCircle(1, 135, 95));
        configuration.setInnerCircle(new SealCircle(2, 85, 45));

        try {
            SealUtil.buildAndStoreSeal(configuration, filePath);
        } catch (Exception e) {
            throw new IOException("保存公章到本地失败", e);
        }
    }

    @Override
    public void savePersonalSealToLocal(String name, String additionalText, String filePath) throws IOException {
        // 私章配置保持不变
        SealFont font = new SealFont(name);
        font.setFontSize(120);
        font.setBold(true);
        font.setFontFamily("宋体");

        try {
            SealUtil.buildAndStorePersonSeal(300, 16, font, additionalText, filePath);
        } catch (Exception e) {
            throw new IOException("保存私章到本地失败", e);
        }
    }

    @Override
    public List<Seal> selectSealList(Seal seal) {
        return SealMapper.selectSealList(seal);
    }

    @Override
    public Seal selectSealById(Long id) {
        return SealMapper.selectSealById(id);
    }

    @Override
    public int insertSeal(Seal seal) {
        return SealMapper.insertSeal(seal);
    }

    @Override
    public int updateSeal(Seal seal) {
        return SealMapper.updateSeal(seal);
    }

    @Override
    public int deleteSealByIds(Long[] ids) {
        return SealMapper.deleteSealByIds(ids);
    }
}