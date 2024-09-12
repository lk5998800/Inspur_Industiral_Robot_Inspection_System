package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 灭火器检测参数(FireExtinguisherPara)实体类
 *
 * @author kliu
 * @since 2022-11-25 11:23:54
 */
@ApiModel("灭火器检测参数实体类")
@ToString
@Data
public class FireExtinguisherPara{
     @ApiModelProperty("机房id")
     private long roomId;
     @ApiModelProperty("点位名称")
     private String pointName;
     @ApiModelProperty("灭火器基础图片url")
     private String fireExitinguisherPath;
     @ApiModelProperty("灭火器特征值")
     private String fireExitinguisherPos;
     @ApiModelProperty("灭火器数量")
     private int fireExitinguisherNum;
}

