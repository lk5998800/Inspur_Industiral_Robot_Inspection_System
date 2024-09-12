package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 地图参数
 * @author kliu
 * @date 2022/11/21 9:21
 */
@Data
@ApiModel("地图参数")
@ToString
public class MapPara {
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("地图url")
  private String url;
  @ApiModelProperty("地图高度")
  private long height;
  @ApiModelProperty("地图宽度")
  private long width;
  @ApiModelProperty("原点坐标X")
  private double originX;
  @ApiModelProperty("原点坐标Y")
  private double originY;
  @ApiModelProperty("resolution")
  private double resolution;
  @ApiModelProperty("offsetX")
  private long offsetX;
  @ApiModelProperty("offsetY")
  private long offsetY;
}
