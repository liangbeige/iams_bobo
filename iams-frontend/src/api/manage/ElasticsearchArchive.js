import request from '@/utils/request'



// 查询档案列表列表
export function ElasticsearchArchiveList(parm) {
    return request({
        url: '/manage/archive/Elasticsearch',
        method: 'get',
        params: parm
    })
}