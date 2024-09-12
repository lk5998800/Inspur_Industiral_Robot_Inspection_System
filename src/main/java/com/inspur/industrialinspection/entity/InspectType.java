package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 巡检类型
 * @author kliu
 * @date 2022/5/24 20:24
 */
@Data
@ApiModel("巡检类型")
@ToString
public class InspectType {
  @ApiModelProperty("巡检类型id")
  private long inspectTypeId;
  @ApiModelProperty("巡检类型名称")
  private String inspectTypeName;
  @ApiModelProperty("巡检模式")
  private String runMode;
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty(value = "机器人id", hidden = true)
  private long robotId;
}
