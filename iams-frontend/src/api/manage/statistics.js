import request from '@/utils/request'
import dayjs from 'dayjs'

// 查询统计数据列表
export function listStatistics(query) {
  return request({
    url: '/manage/statistics/list',
    method: 'get',
    params: query
  })
}

// 查询最新一条数据
export function getLatestStatistics() {
  return request({
    url: '/manage/statistics/latest',
    method: 'get'
  })
}

// 查询统计数据详细
export function getStatistics(id) {
  return request({
    url: '/manage/statistics/' + id,
    method: 'get'
  })
}

// 新增统计数据
export function addStatistics(data) {
  return request({
    url: '/manage/statistics',
    method: 'post',
    data: data
  })
}

// 修改统计数据
export function updateStatistics(data) {
  return request({
    url: '/manage/statistics',
    method: 'put',
    data: data
  })
}

// 删除统计数据
export function delStatistics(id) {
  return request({
    url: '/manage/statistics/' + id,
    method: 'delete'
  })
}

// 获取日统计数据
export function getDailyStatistics(date) {
  return request({
    url: '/manage/statistics/daily',
    method: 'get',
    params: {
      date: dayjs(date).format('YYYY-MM-DD') // 格式化为 YYYY-MM-DD
    }
  })
}

// 获取月统计数据
export function getMonthlyStatistics(date) {
  return request({
    url: '/manage/statistics/monthly',
    method: 'get',
    params: {
      year: date.getFullYear(),
      month: date.getMonth() + 1
    }
  })
}

// 获取最近几天的借出数量数据
export function getRecentBorrowData(days = 7) {
  return request({
    url: '/manage/statistics/recent-borrow',
    method: 'get',
    params: {
      days: days
    }
  })
}
