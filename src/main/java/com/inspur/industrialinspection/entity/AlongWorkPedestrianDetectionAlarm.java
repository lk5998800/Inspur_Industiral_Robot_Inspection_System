package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * 随工行人检测告警信息
 * @author kliu
 * @date 2022/6/28 13:54
 */
@ApiModel("随工行人检测告警信息")
@ToString
@Data
public class AlongWorkPedestrianDetectionAlarm {
  private long id;
  private long pid;
  private String pointName;
  private String alarmTime;
  private String alarmInformation;
  private String url;
}
