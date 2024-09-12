package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author: LiTan
 * @description:  讲解检测点基本信息
 * @date:   2022-10-31 09:52:45
 */
@ApiModel("讲解检测点基本信息")
@ToString
@Data
public class ExplainPointInfo {
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("检测点名称")
  private String pointName;
  @ApiModelProperty("locationX")
  private BigDecimal locationX;
  @ApiModelProperty("locationY")
  private BigDecimal locationY;
  @ApiModelProperty("locationZ")
  private BigDecimal locationZ;
  @ApiModelProperty("orientationX")
  private BigDecimal orientationX;
  @ApiModelProperty("orientationY")
  private BigDecimal orientationY;
  @ApiModelProperty("orientationZ")
  private BigDecimal orientationZ;
  @ApiModelProperty("orientationW")
  private BigDecimal orientationW;
}
