import request from '@/utils/request'
import { pa } from 'element-plus/es/locales.mjs'

// 查询档案列表列表
export function listArchive(query) {
  return request({
    url: '/manage/archive/list',
    method: 'get',
    params: query
  })
}

export function listArchiveAll() {
  return request({
    url: '/manage/archive/list-all',
    method: 'get',
  })
}

// 批量上传盘点结果
export function uploadInventoryResult(data) {
  return request({
    url: '/manage/archive/inventory/upload',
    method: 'post',
    data: data
  })
}

// 根据RFID批量查询档案
export function getArchivesByRfids(rfids) {
  return request({
    url: '/manage/archive/batch-by-rfid',
    method: 'post',
    data: rfids
  })
}

export function addArchiveCategoryRootName(){
  return request({
    url: '/manage/archive/addCategoryRootName',
    method: 'post'
  })
}

export function getArchiveByRfid(rfid) {
  return request({
    url: '/manage/archive/get-by-rfid/' + rfid,
    method: 'get',
  })
}

// 导出盘点结果
export function exportInventoryResult(data) {
  return request({
    url: '/manage/archive/inventory/export',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}

// 查询已借阅档案列表（包含同一卷未借阅但可访问的档案）
export function listBorrowArchiveInVolume(query)
{
  return request({
    url: '/manage/archive/borrowArchiveInVolume',
    method: 'get',
    params: query
  })
}

// 查询档案列表详细
export function getArchive(id) {
  return request({
    url: '/manage/archive/' + id,
    method: 'get'
  })
}

// 查询当前档案的查看权限
export function ArchivePermission(id) {
  return request({
    url: '/manage/archive/permission/' + id,
    method: 'get',
  })
}

// 新增档案列表
export function addArchive(data) {
  return request({
    url: '/manage/archive',
    method: 'post',
    data: data
  })
}

// 修改档案列表
export function updateArchive(data) {
  return request({
    url: '/manage/archive',
    method: 'put',
    data: data
  })
}

// 删除档案列表
export function delArchive(id) {
  return request({
    url: '/manage/archive/' + id,
    method: 'delete'
  })
}

// 新增档案文档目录
export function addDirectoryFiles(data) {
  return request({
    url: '/manage/archive/directory',
    method: 'put',
    data: data
  })
}

// 删除档案文档目录
export function deleteDirectoryFiles(data) {
  return request({
    url: '/manage/archive/directory/delete',
    method: 'put',
    data: data
  })
}

// 根据location查询档案列表
export function listArchiveByLocation(shitiLocation, exactLocation) {
  return request({
    url: '/manage/archive/location',
    method: 'get',
    params: { shitiLocation: shitiLocation, exactLocation: exactLocation }
  })
}

// 拖拽移动档案
export function moveArchive(archiveId, targetLocation) {
  return request({
    url: '/manage/archive/move',
    method: 'post',
    data: { archiveId, targetLocation }
  });
}

// 节层中档案全部下架
export function updateArchivesLocation(archives) {
  return request({
    url: '/manage/archive/updateLocation',
    method: 'post',
    data: archives
  })
}

// 得到未上架的档案列表
export function getUpArchiveList(query) {
  return request({
    url: '/manage/archive/upArchiveList',
    method: 'get',
    params: query
  })
}

// 得到上架的档案列表
export function getArchivedList() {
  return request({
    url: '/manage/archive/getArchivedList',
    method: 'get',
  })
}

// 根据mysqlDanghao获取档案ID
export function getArchiveIdByMysqlDanghao(mysqlDanghao) {
  return request({
    url: '/manage/archive/get-id-by-mysql-danghao',
    method: 'get',
    params: { mysqlDanghao }
  });
}

// 根据mysqlDanghao获取档案实体位置
export function getArchiveLocationByMysqlDanghao(mysqlDanghao) {
  return request({
    url: '/manage/archive/get-location-by-mysql-danghao',
    method: 'get',
    params: { mysqlDanghao }
  });
}

// 获取档案编号
export function getNextArchiveNumber(id) {
  return request({
    url: '/manage/archive/get-next-archive-number',
    method: 'get',
    params: { id }
  });
}

// 获取rfid射频标签
export function getRfid() {
  return request({
    url: '/manage/archive/get-rfid',
    method: 'get',
  });
}

// 文档批量挂接的接口
export function batchUpdateDocumentDirectory(docIds, directoryId) {
  return request({
    url: '/manage/archive/batch-mount',
    method: 'post',
    data: { docIds, directoryId }
  })
}

// 项目档案列表
export function getProjectArchives(params) {
  return request({
    url: '/manage/archive/list',
    method: 'get',
    params: {
      projectId: params.projectId,
      pageNum: params.pageNum,
      pageSize: params.pageSize,

      danghao: params.danghao,           // 档号搜索
      name: params.name,                 // 档案名称搜索
      rootCategoryCode: params.rootCategoryCode,  // 大门类编码
      categoryCode: params.categoryCode  // 小门类编码
    }
  })
}

// 四性检测
export function fourCharacteristicsTest(id) {
  return request({
    url: '/manage/archive/fourCharacteristicsTest/' +  id,
    method: 'post',
  })
}
// API 函数修改
export function guidang(param) {
  return request({
    url: '/manage/archive/guidang',
    method: 'post',
    data: param,
    timeout: 300000
  })
}

export function unarchive(param) {
  return request({
    url: '/manage/archive/unarchive',
    method: 'post',
    data: param
  })
}

export function selectArchiveByCategoryId(query, { paramsSerializer } = {}) {
  return request({
    url: '/manage/archive/selectByCategoryId',
    method: 'get',
    params: query,
    paramsSerializer: paramsSerializer // 可选自定义序列化方式
  })
}

export function printRfid(id) {
  return request({
    url: '/manage/archive/printRFID/'+  id,
    method: 'post',
  })
}
