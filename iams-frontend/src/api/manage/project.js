import request from '@/utils/request'

// 查询项目管理列表
export function listProject(query) {
  return request({
    url: '/manage/project/list',
    method: 'get',
    params: query
  })
}

// 查询项目管理详细
export function getProject(id) {
  return request({
    url: '/manage/project/' + id,
    method: 'get'
  })
}

// 新增项目管理
export function addProject(data) {
  return request({
    url: '/manage/project',
    method: 'post',
    data: data
  })
}

// 修改项目管理
export function updateProject(data) {
  return request({
    url: '/manage/project',
    method: 'put',
    data: data
  })
}

// 删除项目管理
export function delProject(id) {
  return request({
    url: '/manage/project/' + id,
    method: 'delete'
  })
}

// 获取项目所属档案
// export function listProjectArchives(params) {
//   return request({
//     url: '/manage/project/archives',
//     method: 'get',
//     params
//   })
// }
// 获取项目档案列表
export function getProjectArchives(params) {
  return request({
    url: '/manage/project/archives',
    method: 'get',
    params: {
      projectId: Number(params.projectId), // 转换为数字类型
      name: params.name,
      danghao: params.danghao,
      pageNum: params.pageNum,
      pageSize: params.pageSize
    }
  })
}