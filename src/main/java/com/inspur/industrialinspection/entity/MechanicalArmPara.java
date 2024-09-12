package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ApiModel("机械臂参数")
@ToString
public class MechanicalArmPara {
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("机械臂高度")
  private String positionHeight;
  @ApiModelProperty("机械臂参数")
  private String positionParam;
}
