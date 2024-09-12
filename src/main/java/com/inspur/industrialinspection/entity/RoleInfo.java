package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 用户信息
 * @author wangzhaodi
 * @date 2022/11/14 14:55
 */
@ApiModel("角色信息")
@ToString
@Data
public class RoleInfo {
    @ApiModelProperty("角色id")
    private long roleId;
    @ApiModelProperty("角色名")
    private String roleName;
    @ApiModelProperty("园区id")
    private String parkId;
    @ApiModelProperty("中文名称")
    private String chineseName;
    @ApiModelProperty("功能id")
    private String functionId;
}
