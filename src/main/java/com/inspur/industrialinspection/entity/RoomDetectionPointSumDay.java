package com.inspur.industrialinspection.entity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ApiModel("机房点位检测项汇总")
@ToString
public class RoomDetectionPointSumDay {
  private long roomId;
  private String detectionDate;
  private String pointName;
  private String detectionId;
  private BigDecimal max;
  private BigDecimal min;
  private long abnormalCount;
  private long count;
  private String maxAbnormal;
  private String minAbnormal;
}
