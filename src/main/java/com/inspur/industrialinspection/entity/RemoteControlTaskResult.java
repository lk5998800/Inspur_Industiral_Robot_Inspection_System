package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ApiModel("远程控制任务结果")
@ToString
public class RemoteControlTaskResult {
  @ApiModelProperty("任务实例id")
  private long instanceId;
  @ApiModelProperty("图片类型")
  private String imgType;
  @ApiModelProperty("图片url")
  private String imgUrl;
  @ApiModelProperty("图片时间")
  private String imgTime;
}
