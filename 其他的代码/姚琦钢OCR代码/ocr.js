import request from '@/utils/request'
export function doOcr(data) {
  return request({
    url: '/doOcr',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    method: 'post',
    data: data
  })
}