package com.inspur.industrialinspection.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 随工执行记录
 * @author kliu
 * @date 2022/6/14 16:35
 */
@ApiModel("随工执行记录")
@ToString
@Data
public class AlongWorkDtl {
  @ApiModelProperty("id")
  private long id;
  @ApiModelProperty("pid")
  private long pid;
  @ApiModelProperty("点位名称")
  private String pointName;
  @ApiModelProperty("巡检时间")
  private String inspectionTime;
  @ApiModelProperty("图片路径")
  private String imgUrl;
}
