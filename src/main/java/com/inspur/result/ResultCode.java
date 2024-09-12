package com.inspur.result;

/**
 * 状态码
 * @author kliu
 * @date 2022/5/24 18:08
 */
public enum ResultCode {

    /* 成功状态码 */
    SUCCESS(0, "成功"),

    /* 失败状态码 */
    FAIL(1, "失败");

    private Integer code;

    private String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

}