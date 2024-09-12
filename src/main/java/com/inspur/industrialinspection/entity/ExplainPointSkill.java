package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * @author: LiTan
 * @description:  讲解检测点技能信息
 * @date:   2022-10-31 09:52:45
 */
@ApiModel("讲解检测点技能信息")
@ToString
@Data
public class ExplainPointSkill {
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("检测点名称")
  private String pointName;
  @ApiModelProperty("等待时间")
  private Integer waitingTime = 0;
  //@NotBlank(message = "播报内容不能为空")
  @ApiModelProperty("播报内容")
  private String broadcast;
}
