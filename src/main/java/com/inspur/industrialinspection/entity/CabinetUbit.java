package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

@ApiModel("机柜u位")
@ToString
@Data
public class CabinetUbit {
  private long roomId;
  private int ubit;
  private int useUbit;
  private int freeUbit;
  private String pointName;
  private int usageRate;
}
