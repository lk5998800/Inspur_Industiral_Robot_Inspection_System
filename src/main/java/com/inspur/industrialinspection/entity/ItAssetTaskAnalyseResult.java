package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * 资产任务分析结果信息
 * @author kliu
 * @date 2022/7/25 11:20
 */
@Data
@ApiModel("资产任务分析结果信息")
@ToString
public class ItAssetTaskAnalyseResult {
  private long id;
  private long instanceId;
  private long itAssetId;
  private String result;
}
