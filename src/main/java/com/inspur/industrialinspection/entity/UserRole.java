package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 用户-角色关联信息
 * @author wangzhaodi
 * @date 2022/11/14 15:19
 */
@ApiModel("用户-角色关联信息")
@ToString
@Data
public class UserRole {
    @ApiModelProperty("用户-角色关联id")
    private long userRoleId;
    @ApiModelProperty("用户id")
    private long userId;
    @ApiModelProperty("角色id")
    private long roleId;
}
