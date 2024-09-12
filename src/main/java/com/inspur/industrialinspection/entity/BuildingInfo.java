package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 楼栋信息
 * @throws
 * @author kliu
 * @date 2022/6/20 20:24
 */
@ApiModel("楼栋信息")
@ToString
@Data
public class BuildingInfo {
  @ApiModelProperty("楼栋id")
  private long buildingId;
  @ApiModelProperty("楼栋名称")
  private String buildingName;
  @ApiModelProperty("园区id")
  private long parkId;
}
