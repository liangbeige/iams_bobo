import request from '@/utils/request'

// 查询借阅审批列表
export function listApproval(query) {
  return request({
    url: '/manage/approval/list',
    method: 'get',
    params: query
  })
}

// 查询借阅审批详细
export function getApproval(id) {
  return request({
    url: '/manage/approval/' + id,
    method: 'get'
  })
}

// 新增借阅审批
export function addApproval(data) {
  return request({
    url: '/manage/approval',
    method: 'post',
    data: data
  })
}

// 修改借阅审批
export function updateApproval(data) {
  return request({
    url: '/manage/approval',
    method: 'put',
    data: data
  })
}

// 删除借阅审批
export function delApproval(id) {
  return request({
    url: '/manage/approval/' + id,
    method: 'delete'
  })
}
