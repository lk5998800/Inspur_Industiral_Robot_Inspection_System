package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 检测项组合
 * @author kliu
 * @date 2022/5/6 16:41
 */
@ApiModel("园区信息")
@ToString
@Data
public class ParkInfo {
  @ApiModelProperty("园区id")
  private int parkId;
  @ApiModelProperty("园区名称")
  private String parkName;
  @ApiModelProperty("园区拼音")
  private String parkPinyin;
}
