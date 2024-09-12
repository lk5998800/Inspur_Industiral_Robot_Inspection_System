package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 机器人机房信息
 * @author kliu
 * @date 2022/4/18 20:27
 */
@Data
@ApiModel("机器人机房信息")
@ToString
public class RobotRoom {
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("机器人信息")
  private RobotInfo robotInfo;
}
