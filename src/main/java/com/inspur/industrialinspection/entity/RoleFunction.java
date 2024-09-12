package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 角色-功能关联信息
 * @author wangzhaodi
 * @date 2022/11/14 15:19
 */
@ApiModel("角色-功能关联信息")
@ToString
@Data
public class RoleFunction {
    @ApiModelProperty("角色-功能关联id")
    private long roleFunctionId;
    @ApiModelProperty("角色id")
    private long roleId;
    @ApiModelProperty("功能id")
    private long functionId;
}
