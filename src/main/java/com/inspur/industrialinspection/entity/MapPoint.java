package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 地图点位
 * @author kliu
 * @date 2022/6/1 15:19
 */
@Data
@ApiModel("地图点位")
@ToString
public class MapPoint {
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("点位名称")
  private String pointName;
  @ApiModelProperty("点位类型")
  private long pointType;
  @ApiModelProperty("X")
  private long pointX;
  @ApiModelProperty("Y")
  private long pointY;
}
