package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 监测点检测项阈值对应关系
 * @author kliu
 * @date 2022/5/24 20:24
 */
@ApiModel("监测点检测项阈值对应关系")
@ToString
@Data
public class PointDetectionInfo {
  @ApiModelProperty("检测点名称")
  private String pointName;
  @ApiModelProperty("检测项")
  private List<DetectionInfo> detectionList;
}
