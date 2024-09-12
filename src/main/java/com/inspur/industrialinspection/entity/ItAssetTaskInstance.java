package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 资产盘点任务记录信息
 * @author kliu
 * @date 2022/7/25 11:20
 */
@Data
@ApiModel("资产盘点任务记录信息")
@ToString
public class ItAssetTaskInstance {
  @ApiModelProperty("任务实例id")
  private long instanceId;
  @ApiModelProperty("任务id")
  private long taskId;
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("开始时间")
  private String startTime;
  @ApiModelProperty("结束时间")
  private String endTime;
  @ApiModelProperty(value = "机器人名称")
  private String robotName;
  @ApiModelProperty("机房名称")
  private String roomName;
  @ApiModelProperty("执行状态")
  private String execStatus;
  @ApiModelProperty("执行结果")
  private String taskResult;
  @ApiModelProperty("任务名称")
  private String taskName;
  @ApiModelProperty("创建人")
  private String createUserName;
}
