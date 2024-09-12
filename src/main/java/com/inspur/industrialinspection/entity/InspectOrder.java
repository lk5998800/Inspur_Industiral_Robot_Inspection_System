package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 巡检顺序
 * @author kliu
 * @date 2022/5/24 20:24
 */
@ApiModel("巡检顺序")
@ToString
@Data
public class InspectOrder {
  @ApiModelProperty("巡检类型id")
  private long inspectTypeId;
  @ApiModelProperty("检测点名称")
  private String pointName;
  @ApiModelProperty("巡检顺序")
  private String inspectOrder;
}
