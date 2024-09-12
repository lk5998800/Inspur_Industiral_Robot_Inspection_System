package com.inspur.industrialinspection.entity;

import lombok.Data;
import lombok.ToString;

/**
 * 告警短信发送信息
 * @author kliu
 * @date 2022/5/24 20:27
 */
@Data
@ToString
public class WarnSms {
  private long warnSmsId;
  private long taskId;
  private String pointName;
  private long userId;
}
