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
 * @author: LiTan
 * @description:    检测项组合
 * @date:   2022-10-31 09:52:56
 */
@ApiModel("参观讲解管理列表")
@ToString
@Data
public class ExplainTask {
  @ApiModelProperty("id")
  private long id;
  @ApiModelProperty("机房id")
  private long roomId;
  @Pattern(regexp = "^((?!>|<|%).)((?!>|<|%).){0,50}$", message = "任务名称不能包含<,>或%字符，最长为40个字符")
  @Size(max= 40, message = "任务名称长度不能大于40")
  @NotBlank(message = "任务名称不能为空")
  @ApiModelProperty("任务名称")
  private String taskName;
  @Size(max= 20, message = "任务类型长度不能大于20")
  @NotBlank(message = "任务类型不能为空")
  @ApiModelProperty("任务类型")
  private String taskType;
  @Size(max= 20, message = "预约时间长度不能大于20")
  @ApiModelProperty("预约时间")
  private String taskTime;
  @Size(max= 200, message = "任务名称长度不能大于200")
  @ApiModelProperty("任务描述")
  private String taskDescribe;
  @NotBlank(message = "讲解点位不能为空")
  @ApiModelProperty("讲解点位")
  private String points;
  @ApiModelProperty("开始时间")
  private String startTime;
  @ApiModelProperty("结束时间")
  private String endTime;
  @ApiModelProperty("任务状态")
  private String status;
  @ApiModelProperty("终止原因")
  private String reason;
  @ApiModelProperty("创建时间")
  private String createTime;
  @ApiModelProperty("创建人Id")
  private long createUserId;
}
