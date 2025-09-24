package com.iams.minio.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * controller的返回结果统一使用该类进行包装
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class R {

    private int code;

    private String msg;

    private Object data;

    public static R OK() {
        return new R(200, "成功", null);
    }

    public static R OK(Object data) {
        return new R(200, "成功", data);
    }

    public static R OK(String url, Object data) {
        return new R(200, url, data);
    }

    public static R OK(String url) {
        return new R(200, url,  null);
    }

    public static R FAIL() {
        return new R(500, "失败", null);
    }

    public static R FAIL(String info) {
        return new R(500, info, null);
    }

}
