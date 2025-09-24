package com.iams.manage.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.iams.manage.domain.Archive;
import com.iams.manage.domain.Document;
import com.iams.manage.domain.InventoryDTO.InventoryResultDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 档案列表Service接口
 *
 * @author zhjm
 * @date 2025-01-10
 */
public interface IArchiveService
{
    /**
     * 查询档案列表
     *
     * @param id 档案列表主键
     * @return 档案列表
     */
    public Archive selectArchiveById(Long id);
    List<Archive> selectArchiveListByStatuses(Archive archive, List<String> statuses);
    /**
     * 查询已借阅档案以及同属一卷的档案列表
     *
     * @param archive 档案列表
     * @return 档案列表集合
     */
    public List<Archive> borrowArchiveInVolume(Archive archive);

    /**
     * 查询档案列表列表
     *
     * @param archive 档案列表
     * @return 档案列表集合
     */
    public List<Archive> selectArchiveList(Archive archive);


    /**
     * 查询档案列表列表
     *
     * @param archive 档案列表
     * @return 档案列表集合
     */
    public List<Archive> selectArchiveListAll(Archive archive);

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
     * 批量删除档案列表
     *
     * @param ids 需要删除的档案列表主键集合
     * @return 结果
     */
    public int deleteArchiveByIds(Long[] ids);

    /**
     * 删除档案列表信息
     *
     * @param id 档案列表主键
     * @return 结果
     */
    public int deleteArchiveById(Long id);

    /**
     * 新增文档目录中的文件
     *
     * @param document
     * @return 结果
     */
    public int addDirectoryFiles(Document document);

    /**
     * 删除档案目录中的文件
     *
     * @param document
     * @return 结果
     */
    public int deleteDirectoryFiles(Document document);


    /**
     * 上传销毁佐证材料
     *
     * @param archiveId 档案ID
     * @param file 上传的文件
     * @return 文件访问URL
     * @throws Exception 上传过程中可能发生的异常
     */
    public String uploadDestructionCertificate(Long archiveId, MultipartFile file) throws Exception;



    /**
     * 根据location查询档案列表
     *
     * @param location
     * @return 结果
     */
    List<Archive> getArchiveByLocation(String location);

    int moveArchive(Long archiveId, String targetLocation);

    int updateLocation(List<Archive> archives);

    List<Archive> getUpArchiveList(Map<String, Object> query);

    boolean checkArchivePermission(Long archiveID);

    /**
     * 根据 danghao 获取档案的 id
     */
    Long getArchiveIdByMysqlDanghao(String mysqlDanghao);

    Map<String, Object> getArchiveLocationByDanghao(String mysqlDanghao);


    public void insertPendingEvaluation();

    /**
     * 统计指定项目的档案数量
     *
     * @param projectId 项目ID
     * @return 档案数量
     */
    Long countByProjectId(Long projectId);

    /**
     * 获取一组档案所属的项目ID集合
     *
     * @param archiveIds 档案ID数组
     * @return 项目ID集合
     */
    Set<Long> getProjectIdsForArchives(Long[] archiveIds);


//    // 按项目查询档案
//    List<Archive> selectArchiveByProjectId(Long projectId);
//
//    // 项目档案统计
//    List<Map<String, Object>> countArchiveByProject();

    //四性检测的报告
    public Map<String, Object> generateFourCharacteristicsReport(Long id);

    public boolean guidang(Long id, String config);


    String getDanghaoByDocumentId(Long documentId);

    List<Archive> selectByCategoryId(String categoryId, Long projectId);

    /**
     * 批量导入档案
     *
     * @param archives 档案列表
     * @param projectId 项目ID
     * @return 导入成功的记录数
     */
    int batchImportArchives(List<Archive> archives, Long projectId);

    public boolean checkRfidUnique(String rfid);

    /**
     * 保存盘点主记录
     * @param inventoryResult 盘点结果
     * @return 盘点ID
     */
    Long saveInventoryMain(InventoryResultDTO inventoryResult);

    /**
     * 保存盘点明细
     * @param inventoryId 盘点ID
     * @param records 盘点记录列表
     */
    void saveInventoryDetails(Long inventoryId, List<InventoryResultDTO.InventoryRecord> records);


    /**
     * 根据档号列表批量查询档案状态信息
     * @param danghaos 档号列表
     * @return 状态信息列表
     */
    List<Map<String, Object>> getArchiveStatusByDanghaos(List<String> danghaos);

    Archive selectArchiveByRfid(String rfid);

    List<Archive> getArchivedList();

    public boolean addCategoryRootName();
}
