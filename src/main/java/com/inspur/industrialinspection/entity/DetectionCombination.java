package com.inspur.industrialinspection.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 检测项组合
 * @author kliu
 * @date 2022/4/28 8:29
 */
@ApiModel("检测项组合")
@ToString
@Data
public class DetectionCombination {
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("组合代号")
  private String combinationCode;
  @ApiModelProperty("组合名称")
  private String combinationName;
  @ApiModelProperty("组合明细")
  private List<String> combinationDetl;
  @ApiModelProperty("组合明细字符串")
  private String detectionGroupDetlStr;
  @ApiModelProperty("阈值")
  private String threshold;
}
