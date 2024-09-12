package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@ApiModel("远程遥控任务")
@ToString
@Data
/**
 * 远程遥控实例
 * @author kliu
 * @date 2022/9/14 13:58
 */
public class RemoteControlTaskInstance {
  @ApiModelProperty("任务实例id")
  private long instanceId;
  @ApiModelProperty("开始时间")
  private String startTime;
  @ApiModelProperty("结束时间")
  private String endTime;
  @ApiModelProperty("执行状态")
  private String execStatus;
  @ApiModelProperty("用户id")
  private long userId;
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("机房名称")
  private String roomName;
  @ApiModelProperty("机器人id")
  private long robotId;
  @ApiModelProperty("图片数量")
  private long picCount;
}
