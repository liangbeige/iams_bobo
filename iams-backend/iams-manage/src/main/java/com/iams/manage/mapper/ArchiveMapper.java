package com.iams.manage.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.iams.manage.domain.Archive;
import com.iams.manage.domain.Document;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 档案列表Mapper接口
 *
 * @author zhjm
 * @date 2025-01-10
 */
@Mapper
public interface ArchiveMapper
{
    /**
     * 查询档案列表
     *
     * @param id 档案列表主键
     * @return 档案列表
     */
    public Archive selectArchiveById(Long id);


    /**
     * 查询档案列表列表
     *
     * @param archive 档案列表
     * @return 档案列表集合
     */
    public List<Archive> selectArchiveList(Archive archive);

    /**
     * 新增档案列表
     *
     * @param archive 档案列表
     * @return 结果
     */
    public int insertArchive(Archive archive);

    /**
     * 修改档案列表
     *
     * @param archive 档案列表
     * @return 结果
     */
    public int updateArchive(Archive archive);

    /**
     * 删除档案列表
     *
     * @param id 档案列表主键
     * @return 结果
     */
    public int deleteArchiveById(Long id);

    /**
     * 批量删除档案列表
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteArchiveByIds(Long[] ids);

    /**
     * 修改档案目录中的文件
     *
     * @param archive 文档
     * @return 结果
     */
    public int addDirectoryFiles(Archive archive);

    /**
     * 删除档案目录中的文件
     *
     * @param archive 档案
     * @return 结果
     */
    public int deleteDirectoryFiles(Archive archive);

    /**
     * 根据location获取档案列表
     *
     * @param shitiLocation,exactLocation 档案柜位置('1-1-A','1-1')
     * @return 结果
     */
    public List<Archive> getArchiveByLocation(String shitiLocation, String exactLocation);

    /**
     * 根据location获取档案列表
     *
     * @param shitiLocation 档案柜位置(1号库房第2排第2列)
     * @return 结果
     */
    public List<Archive> getListByLocation(String shitiLocation);

    /**
     * 查询未上架的档案列表
     */
    List<Archive> selectUpArchiveList(Map<String, Object> query);


    /**
     * 更新档案的销毁佐证材料信息
     *
     * @param archive 包含ID和佐证材料信息的档案对象
     * @return 影响的行数
     */
    public int updateArchiveCertificateInfo(Archive archive);


    /**
     * 根据 danghao 获取档案
     */
//    Archive selectArchiveByDanghao(@Param("danghao") String danghao);

    @Select("SELECT id FROM tb_archive WHERE danghao = #{danghao}")
    Long getArchiveIdByDanghao(String danghao);

    List<Archive> selectArchiveListByStatuses(
            @Param("archive") Archive archive,
            @Param("statuses") List<String> statuses
    );
    Archive selectArchiveByDanghao(String danghao);


    @Select("SELECT shiti_location,exact_location FROM tb_archive WHERE danghao = #{danghao}")
    Map<String, Object> getArchiveLocationByDanghao(String danghao);

    // 根据 id 查询档号
    @Select("SELECT danghao FROM tb_archive WHERE id = #{archiveId}")
    String getDanghaoById(@Param("archiveId") Long archiveId);


    public Long getArchiveIdByRfid(String rfid);

    List<Archive> selectArchiveByProjectId(Long projectId);

    /**
     * 统计指定项目的档案数量
     *
     * @param projectId 项目ID
     * @return 档案数量
     */
    Long countByProjectId(@Param("projectId") Long projectId);


    /**
            * 根据ID集合查询档案列表
     * @param ids 档案ID数组
     * @return 档案列表
     */
    List<Archive> selectArchiveListByIds(Long[] ids);

    @MapKey("id") // 直接匹配数据库字段名
    Map<Integer, Map<String, Object>> selectDanghaoMapByIds(@Param("archiveIds") Set<Long> archiveIds);
    /**
     * 根据
     */

    /**
     * 根据档号列表批量查询档案状态和位置信息
     * @param danghaos 档号列表
     * @return 档案状态信息列表
     */
    @Select({
            "<script>",
            "SELECT danghao, status, shiti_location, exact_location",
            "FROM tb_archive",
            "WHERE danghao IN",
            "<foreach collection='list' item='danghao' open='(' separator=',' close=')'>",
            "#{danghao}",
            "</foreach>",
            "</script>"
    })
    List<Map<String, Object>> selectArchiveStatusByDanghaos(@Param("list") List<String> danghaos);



    List<Archive> selectArchiveListByUserBorrow(Long userId);

    List<Archive> selectByCategoryId(String categoryId, Long projectId);

    List<Archive> selectBorrowArchiveInVolume(Archive archive, Long userId);

    /**
     * 检查档号是否已存在
     * @param danghao 档号
     * @return 存在数量
     */
    int checkDanghaoExists(String danghao);



    /**
     * 批量检查档号是否存在
     * @param danghaos 档号集合
     * @return 存在的档号数量
     */
    int checkDanghaoExistsBatch(@Param("danghaos") Set<String> danghaos);

    /**
     * 批量获取已存在的档号列表
     * @param danghaos 档号集合
     * @return 已存在的档号列表
     */
    List<String> getExistingDanghaos(@Param("danghaos") Set<String> danghaos);

    /**
     * 批量检查档号是否存在（排除指定ID）
     * @param danghaos 档号集合
     * @param excludeId 要排除的档案ID
     * @return 存在的档号数量
     */
    int checkDanghaoExistsBatchExcludeId(@Param("danghaos") Set<String> danghaos, @Param("excludeId") Long excludeId);

    /**
     * 批量获取已存在的档号列表（排除指定ID）
     * @param danghaos 档号集合
     * @param excludeId 要排除的档案ID
     * @return 已存在的档号列表
     */
    List<String> getExistingDanghaosExcludeId(@Param("danghaos") Set<String> danghaos, @Param("excludeId") Long excludeId);

    int batchInsertArchives(List<Archive> archives);

    int checkRfidDuplicate(String rfid);

    Archive selectArchiveByRfid(String rfid);

    List<Archive> getArchivedList();
}
