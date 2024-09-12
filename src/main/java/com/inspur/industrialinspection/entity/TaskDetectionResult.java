package com.inspur.industrialinspection.entity;

import lombok.Data;

/**
 * 任务检测项结果
 * @author kliu
 * @date 2022/4/18 20:28
 */
@Data
public class TaskDetectionResult {
  private long taskLogId;
  private long instanceId;
  private String pointName;
  private String sensor;
  private String infrared;
  private String alarmLight;
  private String fireExtinguisher;
  private String analyseFlag;
  private String updateTime;
}
