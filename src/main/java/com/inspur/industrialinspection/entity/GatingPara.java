package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * 门控参数
 * @author kliu
 * @date 2022/6/23 19:11
 */
@ApiModel("门控参数")
@ToString
@Data
public class GatingPara {
  private String pointName;
  private String doorCode;
  private String requestOrder;
  private long roomId;
}
