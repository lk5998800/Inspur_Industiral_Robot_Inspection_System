package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * 特定点巡检任务
 * @author kliu
 * @date 2022/9/7 17:30
 */
@ApiModel("特定点巡检任务")
@ToString
@Data
public class ParticularPointInspectionTaskInstance {
  @ApiModelProperty("任务实例id")
  private long instanceId;
  @ApiModelProperty("开始时间")
  private String startTime;
  @ApiModelProperty("结束时间")
  private String endTime;
  @ApiModelProperty("执行状态")
  private String execStatus;
  @NotBlank(message = "检测项不能为空")
  @ApiModelProperty("检测项 front_camera 前置相机  after_camera 后置相机 industry_camera 工业相机 infrared_camera 红外相机")
  private String detection;
  @NotBlank(message = "点位名称不能为空")
  @ApiModelProperty("点位名称")
  private String pointName;
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("机房名称")
  private String roomName;
  @ApiModelProperty("机器人id")
  private long robotId;
  @ApiModelProperty("相册数量")
  private long picCount;
}
