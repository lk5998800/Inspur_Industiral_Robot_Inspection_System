package com.inspur.industrialinspection.entity;

import lombok.Data;

/**
 * 机器人上传数据异常信息
 * @author kliu
 * @date 2022/4/18 20:28
 */
@Data
public class RobotUploadException {
  private long exceptionId;
  private String exceptionType;
  private long taskId;
  private long robotId;
  private long roomId;
  private String exceptionTime;
  private String poinName;
  private String exceptionDesc;
  private long lifterHeight;
}
