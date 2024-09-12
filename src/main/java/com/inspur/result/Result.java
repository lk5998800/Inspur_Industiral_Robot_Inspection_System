package com.inspur.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用返回结果
 * @author kliu
 * @date 2022/5/24 18:05
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel("Result")
public class Result<T> {
    /**
     * 1.status状态值：代表本次请求response的状态结果。
     */
    @ApiModelProperty("状态码 0 成功  1失败  2 token过期")
    private Integer code;
    /**
     * 2.response描述：对本次状态码的描述。
     */
    @ApiModelProperty("描述")
    private String message;
    /**
     * 3.data数据：本次返回的数据。
     */
    @ApiModelProperty("数据")
    private T data;

    /**
     * 成功，创建ResResult：没data数据
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/5/24 18:06
     */
    public static Result success() {
        Result result = new Result();
        result.setResultCode(ResultCode.SUCCESS);
        return result;
    }

    /**
     * 成功，创建ResResult：有data数据
     * @param data
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/5/24 18:06
     */
    public static Result success(Object data) {
        Result result = new Result();
        result.setResultCode(ResultCode.SUCCESS);
        result.setResultData(data);
        return result;
    }

    /**
     * 失败，指定状态、描述
     * @param status
     * @param desc
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/5/24 18:06
     */
    public static Result fail(Integer status, String desc) {
        Result result = new Result();
        result.setResultCode(status);
        result.setResutMessage(desc);
        return result;
    }

    /**
     * com.inspur.result.Result
     * @param desc
     * @author kliu
     * @description 失败，指定描述
     * @date 2022/5/24 18:06
     */
    public static Result fail(String desc) {
        Result result = new Result();
        result.setResultCode(1);
        result.setResutMessage(desc);
        return result;
    }

    /**
     * token失效的返回
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/5/24 18:06
     */
    public static Result tokenInvalid() {
        Result result = new Result();
        result.setResultCode(2);
        result.setResutMessage("用户登录超时，请重新登录");
        return result;
    }

    /**
     * 失败，指定ResultCode枚举
     * @param resultCode
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/5/24 18:07
     */
    public static Result fail(ResultCode resultCode) {
        Result result = new Result();
        result.setResultCode(resultCode);
        return result;
    }

    /**
     * 自定义返回参数
     * @param code,data,msg
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/5/24 18:07
     */
    public static Result success(int code,Object data,String msg) {
        Result result = new Result();
        result.setResultCode(code);
        result.setResultData(data);
        result.setResutMessage(msg);
        return result;
    }

    private void setResultCode(ResultCode code) {
        this.code = code.code();
        this.message = code.message();
    }
    private void setResultCode(int code) {
        this.code = code;
    }

    private void setResutMessage(String message) {
        this.message = message;
    }

    private void setResultData(T data) {
        this.data = data;
    }
}