// src/api/system/seal.js
import request from '@/utils/request'

// 查询印章列表
export function listSeal(query) {
    return request({
        url: '/manage/seal/list',
        method: 'get',
        params: query
    })
}

// 查询印章详细
export function getSeal(id) {
    return request({
        url: '/manage/seal/' + id,
        method: 'get'
    })
}

// 新增印章
export function addSeal(data) {
    // 确保传递完整的DTO结构
    const postData = {
        ...data,
        sealType: data.sealType || (data.additionalText ? 'PERSONAL' : 'OFFICIAL')
    };
    return request({
        url: '/manage/seal/add',
        method: 'post',
        data: data
    })
}



// 根据文档ID生成公章
export function generateOfficialByDocument(documentId) {
    return request({
        url: '/manage/seal/official/generateByDocument',
        method: 'get',
        params: { documentId },
        headers: {
            'Accept': 'image/png' // 明确指定接受PNG格式
        },
        responseType: 'blob' // 关键：处理二进制响应
    })

}

// // 预览印章
// export function previewSeal(data) {
//     return request({
//         url: '/manage/seal/preview',
//         method: 'post',
//         data: data,
//         responseType: 'arraybuffer'
//     })
// }
// 公章预览
export function previewOfficialSeal(params) {
    return request({
        url: '/manage/seal/official/preview',  // 匹配后端路径
        method: 'GET',
        params: params,
        // responseType: 'arraybuffer'
        responseType: 'json' // 明确要求JSON
    })
}
// 私章预览
export function previewPersonalSeal(params) {
    return request({
        url: '/manage/seal/personal/preview',  // 匹配后端路径
        method: 'GET',
        params: params,
        // responseType: 'arraybuffer'
        responseType: 'json' // 明确要求JSON
    })
}
