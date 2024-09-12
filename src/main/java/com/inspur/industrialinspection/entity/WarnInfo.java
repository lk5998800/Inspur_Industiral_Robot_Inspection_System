package com.inspur.industrialinspection.entity;

import lombok.Data;

/**
 * 告警信息
 * @author kliu
 * @date 2022/4/18 20:29
 */
@Data
public class WarnInfo {

  private long warnId;
  private long taskLogId;
  private String pointName;
  private String detectionId;
  private String level;
  private String warnTime;
}
