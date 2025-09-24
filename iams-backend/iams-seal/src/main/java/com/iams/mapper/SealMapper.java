package com.iams.mapper;

import com.iams.domain.Seal;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

public interface SealMapper {
    /**
     * 查询印章列表
     */
    List<Seal> selectSealList(Seal seal);

    /**
     * 根据ID查询印章
     */
    Seal selectSealById(Long id);

    /**
     * 新增印章
     */
    int insertSeal(Seal seal);

    /**
     * 修改印章
     */
    int updateSeal(Seal seal);

    /**
     * 批量删除印章
     */
    int deleteSealByIds(Long[] ids);

    /**
     * 插入印章
     */
    @Insert("INSERT INTO tb_seal(sealType, sealName, mainText, viceText, centerText, additionalText) " +
            "VALUES(#{sealType}, #{sealName}, #{mainText}, #{viceText}, #{centerText}, #{additionalText})")
    int insert(Seal seal);

    int updateById(Seal seal);
}