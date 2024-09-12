package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 机器人信息
 * @author kliu
 * @date 2022/5/24 20:25
 */
@Data
@ApiModel("机器人信息")
@ToString
public class RobotInfo {
  @ApiModelProperty("机器人id")
  private long robotId;
  @ApiModelProperty("机器人名称")
  private String robotName;
  @ApiModelProperty("是否在用")
  private String inUse;
}
