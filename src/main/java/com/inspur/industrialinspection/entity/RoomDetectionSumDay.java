package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 检测项汇总信息
 * @author kliu
 * @date 2022/5/24 20:25
 */
@ApiModel("检测项汇总信息")
@ToString
@Data
public class RoomDetectionSumDay {
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("检测日期")
  private String detectionDate;
  @ApiModelProperty("检测项id")
  private String detectionId;
  @ApiModelProperty("平均数")
  private BigDecimal avg;
  @ApiModelProperty("最大值")
  private BigDecimal max;
  @ApiModelProperty("最小值")
  private BigDecimal min;
  @ApiModelProperty("中位数")
  private BigDecimal median;
  @ApiModelProperty("异常点位数量")
  private long abnormalCount;
  @ApiModelProperty("检测点位")
  private long count;
}
