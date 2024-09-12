package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * 机器人电量实体
 * @author kliu
 * @date 2022/8/9 8:45
 */
@ApiModel("机器人电量")
@ToString
@Data
public class RobotPower {
  private long robotId;
  private double power;
  private String heartTime;
  private int heartDay;
}
