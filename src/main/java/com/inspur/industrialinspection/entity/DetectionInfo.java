package com.inspur.industrialinspection.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 检测项信息实体
 * @author kliu
 * @date 2022/4/28 8:31
 */
@ApiModel("检测项信息")
@ToString
@Data
public class DetectionInfo {
  @ApiModelProperty("检测项id")
  private String detectionId;
  @ApiModelProperty("检测项名称")
  private String detectionName;
  @ApiModelProperty("机器人执行检测项")
  private String robotDetection;
  @ApiModelProperty("阈值")
  private String threshold;
  @ApiModelProperty("升降杆高度")
  private int lifterHeight;
}
