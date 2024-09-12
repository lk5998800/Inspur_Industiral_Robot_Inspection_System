package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 检测点关联检测项组合
 * @author kliu
 * @date 2022/5/24 20:23
 */
@ApiModel("检测点关联检测项组合")
@ToString
@Data
public class InspectDetection {
  @ApiModelProperty("巡检类型id")
  private long inspectTypeId;
  @ApiModelProperty("检测点名称")
  private String pointName;
  @ApiModelProperty("组合id")
  private String groupNo;
}
