import request from '@/utils/request'

// 查询借阅记录列表
export function listRecord(query) {
  return request({
    url: '/manage/record/list',
    method: 'get',
    params: query
  })
}


export function listRecordOverdue(query) {
  return request({
    url: '/manage/record/overdue',
    method: 'get',
    params: query
  })
}

export function RemindOverdue(query) {
  return request({
    url: '/manage/record/remind',
    method: 'put',
    data: query
  })
}

// 查询借阅记录详细
export function getRecord(id) {
  return request({
    url: '/manage/record/' + id,
    method: 'get'
  })
}

// 新增借阅记录
export function addRecord(data) {
  return request({
    url: '/manage/record',
    method: 'post',
    data: data
  })
}

// 修改借阅记录
export function updateRecord(data) {
  return request({
    url: '/manage/record',
    method: 'put',
    data: data
  })
}

// 删除借阅记录
export function delRecord(id) {
  return request({
    url: '/manage/record/' + id,
    method: 'delete'
  })
}

// 催办
export function expediteRecord(id){
  return request({
    url: '/manage/record/expedite/'+id,
    method: 'put'
  })
}

// 手动提前归还
export function advanceRecord(id){
  return request({
    url: '/manage/record/advance/'+id,
    method: 'put'
  })
}

// 借出档案的列表（便于手动归还，单独准备页面与函数）
export function listApprovedRecord(query) {
  return request({
    url: '/manage/record/loanList',
    method: 'get',
    params: query
  })
}

// 直接借阅按钮
export function directRecord(data) {
  return request({
    url: '/manage/record/direct',
    method: 'post',
    data: data
  })
}