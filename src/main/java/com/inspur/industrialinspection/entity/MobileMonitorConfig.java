package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ApiModel("地图点位")
@ToString
public class MobileMonitorConfig {
  @ApiModelProperty("机器人id")
  private long robotId;
  @ApiModelProperty("服务url")
  private String serviceUrl;
}
