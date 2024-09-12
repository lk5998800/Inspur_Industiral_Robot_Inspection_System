package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * 任务执行实例
 * @author kliu
 * @date 2022/5/24 20:26
 */
@Data
@ApiModel("任务执行实例")
@ToString
public class TaskInstance {
  @ApiModelProperty("任务实例id")
  private long instanceId;
  @ApiModelProperty("任务id")
  private long taskId;
  @ApiModelProperty("巡检类型id")
  private long inspectTypeId;
  @ApiModelProperty("巡检类型名称")
  private String inspectTypeName;
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("开始时间")
  private String startTime;
  @ApiModelProperty("结束时间")
  private String endTime;
  @ApiModelProperty(value = "机器人名称")
  private String robotName;
  @ApiModelProperty(value = "机器人id")
  private long robotId;
  @ApiModelProperty("机房名称")
  private String roomName;
  @ApiModelProperty("执行状态")
  private String execStatus;
  @ApiModelProperty("任务json压缩内容")
  private String taskJsonCompress;
  @ApiModelProperty("循环周期")
  private String cycleType;
}
