package com.inspur.industrialinspection.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 用户信息
 * @author kliu
 * @date 2022/5/24 20:27
 */
@ApiModel("用户信息")
@ToString
@Data
public class UserInfo {
  @ApiModelProperty("用户id")
  private long userId;
  @ApiModelProperty("登录名")
  private String loginName;
  @ApiModelProperty("用户名")
  private String userName;
  @ApiModelProperty("用户密码")
  private String userPwd;
  @ApiModelProperty("用户邮箱")
  private String userEmail;
  @ApiModelProperty("用户手机")
  private String userTel;
  @ApiModelProperty("是否在用")
  private String inUse;
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("园区id")
  private int parkId;
  @ApiModelProperty("token")
  private String token;
  @ApiModelProperty("用户头像url")
  private String faceProfileUrl;
  @ApiModelProperty("人脸特征")
  private String facialFeature;
}
