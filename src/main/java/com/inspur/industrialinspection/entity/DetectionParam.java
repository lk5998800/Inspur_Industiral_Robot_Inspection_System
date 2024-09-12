package com.inspur.industrialinspection.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 检测项参数
 * @author kliu
 * @date 2022/5/24 20:23
 */
@ApiModel("检测项参数")
@ToString
@Data
public class DetectionParam {
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("检测项id")
  private String detectionId;
  @ApiModelProperty("检测项名称")
  private String detectionName;
  @ApiModelProperty("检测项预警阈值")
  private String threshold;
  @ApiModelProperty("检测项阈值")
  private List<Threshold> thresholdList;
  @ApiModelProperty("红外测温升降杆高度")
  private List<Integer> infraredHeightList;
}
