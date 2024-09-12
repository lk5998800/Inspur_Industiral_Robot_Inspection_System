package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 功能信息
 * @author wangzhaodi
 * @date 2022/11/14 14:55
 */
@ApiModel("功能信息")
@ToString
@Data
public class FunctionInfo {
    @ApiModelProperty("功能id")
    private long functionId;
    @ApiModelProperty("功能英文名")
    private String englishName;
    @ApiModelProperty("功能中文名")
    private String chineseName;
}
