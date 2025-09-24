import request from '@/utils/request';

export function getArchiveClassCount() {
    return request({
        url: '/archive/classCount',
        method: 'get',
    });
}

export function getBrowseCount(params) {
    return request({
        url: '/browse/count',
        method: 'get',
        params
    });
}