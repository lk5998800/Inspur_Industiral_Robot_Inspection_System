package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * 图片
 * @author wangzhaodi
 * @date 2022/11/10 17:54
 */

@ApiModel("图像")
@ToString
@Data
public class UniappPhoto {
    @ApiModelProperty("图片id")
    private long uniappId;

    @ApiModelProperty("图片url")
    private String imgUrl;

    @ApiModelProperty("机房名称")
    private String roomName;

    @ApiModelProperty("图片使用类型")
    private String imgUseType;

    @ApiModelProperty("时间")
    private String time;

    @ApiModelProperty("巡检任务id")
    private long taskInspectId;

    @ApiModelProperty("点位名称")
    private String pointName;
}
