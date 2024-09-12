package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ApiModel("机器人告警信息")
@ToString
public class RobotWarnSms {
  @ApiModelProperty("id")
  private long id;
  @ApiModelProperty("机器人id")
  private long robotId;
  @ApiModelProperty("发送时间")
  private String smsTime;

}
