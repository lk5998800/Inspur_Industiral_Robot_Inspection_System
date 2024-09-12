package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 资产盘点任务信息
 * @author kliu
 * @date 2022/7/25 11:20
 */
@Data
@ApiModel("资产盘点任务信息")
@ToString
public class ItAssetTaskInfo {
  @ApiModelProperty("任务id")
  private long id;
  @Pattern(regexp = "^((?!>|<|%).)((?!>|<|%).){0,20}$", message = "任务名称不能包含<,>或%字符，最长为20个字符")
  @Size(max= 20, message = "任务名称长度不能大于20")
  @NotBlank(message = "任务名称不能为空")
  @ApiModelProperty("任务名称")
  private String taskName;
  @Min(value = 1, message = "机房id不能为空")
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("机房名称")
  private String roomName;
  @Min(value = 1, message = "机器人id不能为空")
  @ApiModelProperty("机器人id")
  private long robotId;
  @ApiModelProperty("机器人名称")
  private long robotName;
  @Size(max= 10, message = "执行类型长度不能大于10")
  @NotBlank(message = "执行类型不能为空")
  @ApiModelProperty("执行类型")
  private String execType;
  @NotBlank(message = "执行时间不能为空")
  @Size(max= 20, message = "执行时间长度不能大于20")
  @ApiModelProperty("执行时间")
  private String execTime;
  @NotBlank(message = "盘点方式不能为空")
  @Size(max= 10, message = "盘点方式长度不能大于10")
  @ApiModelProperty("盘点方式")
  private String inventoryMethod;
  @Size(max= 200, message = "任务描述长度不能大于200")
  @ApiModelProperty("任务描述")
  private String taskDesc;
  private String createTime;
  @Size(max= 10, message = "循环类型长度不能大于10")
  @ApiModelProperty("循环类型")
  private String cycleType;
  @ApiModelProperty("循环值，每周、每两周、每月需要填该值，工作日每天不需要")
  private long cycleValue;
  @ApiModelProperty("创建人")
  private String createUser;
  @ApiModelProperty("下一次执行时间")
  private String nextExecTime;
}
