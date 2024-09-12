package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 检测点设置
 * @author kliu
 * @date 2022/5/24 20:25
 */
@ApiModel("检测点设置")
@ToString
@Data
public class PointSet {
  @ApiModelProperty("检测点名称")
  private String pointName;
  @ApiModelProperty("组合代号")
  private String groupNo;
  @ApiModelProperty("巡检顺序")
  private String inspectOrder;
  @ApiModelProperty("是否设置位姿")
  private boolean postureFlag;
}
