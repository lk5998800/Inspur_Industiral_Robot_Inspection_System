package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@ApiModel("特定点巡检结果")
@ToString
@Data
public class ParticularPointInspectionTaskResult {
  @ApiModelProperty("实例id")
  private long instanceId;
  @ApiModelProperty("点位名称")
  private String pointName;
  @ApiModelProperty("传感器")
  private String sensor;
  @ApiModelProperty("红外")
  private String infrared;
  @ApiModelProperty("报警灯")
  private String alarmLight;
  @ApiModelProperty("前置")
  private String front;
  @ApiModelProperty("后置")
  private String after;
}
