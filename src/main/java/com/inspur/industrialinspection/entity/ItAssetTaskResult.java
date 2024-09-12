package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * 资产盘点任务结果
 * @author kliu
 * @date 2022/7/28 20:22
 */
@Data
@ApiModel("资产盘点任务结果")
@ToString
public class ItAssetTaskResult {
  private long taskLogId;
  private long instanceId;
  private String pointName;
  private String qrCode;
}
