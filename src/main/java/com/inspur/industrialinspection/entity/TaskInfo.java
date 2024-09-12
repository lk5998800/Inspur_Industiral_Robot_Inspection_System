package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Size;

/**
 * 任务信息
 * @author kliu
 * @date 2022/5/24 20:26
 */
@Data
@ApiModel("任务信息")
@ToString
public class TaskInfo {
  @ApiModelProperty("任务id")
  private long taskId;
  @ApiModelProperty("巡检类型id")
  private long inspectTypeId;
  @ApiModelProperty("巡检类型名称")
  private String inspectTypeName;
  @ApiModelProperty("机器人id")
  private long robotId;
  @ApiModelProperty(value = "机器人名称")
  private String robotName;
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("机房名称")
  private String roomName;
  @Size(max= 500, message = "传入执行时间间隔过短，请检查传入的数据")
  @ApiModelProperty("执行时间 HH:ss 用逗号分隔")
  private String execTime;
  @ApiModelProperty("是否在用")
  private String inUse;
}
