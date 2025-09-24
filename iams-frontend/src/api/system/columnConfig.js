import axios from 'axios'


// 查询固定列配置列表
export function listColumn(ip,port) {
  return axios({
    // url: 'http://固定列Ip地址:端口/MjjWebApi?Op=GetQuXX',
    url: 'http://'+ip+':'+port+'/MjjWebApi?Op=GetQuXX',
    method: 'get',
    // params: query
  })
}

// 查询固定列配置详细
export function getColumn(quNo) {
  return axios({
    url: '/system/column/' + quNo,
    method: 'get'
  })
}

// 新增固定列配置
export function addColumn(data) {
  return axios({
    url: '/system/column',
    method: 'post',
    data: data
  })
}

// 修改固定列配置
export function updateColumn(data) {
  return axios({
    url: '/system/column',
    method: 'put',
    data: data
  })
}

// 删除固定列配置
export function delColumn(quNo) {
  return axios({
    url: '/system/column/' + quNo,
    method: 'delete'
  })
}
