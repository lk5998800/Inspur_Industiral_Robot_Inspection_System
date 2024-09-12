package com.inspur.industrialinspection.entity;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 任务检测项汇总
 * @author kliu
 * @date 2022/4/18 20:28
 */
@Data
public class TaskDetectionSum {
  private long instanceId;
  private String detectionId;
  private BigDecimal avg;
  private BigDecimal max;
  private BigDecimal min;
  private BigDecimal median;
  private long abnormalCount;
  private long count;
}
