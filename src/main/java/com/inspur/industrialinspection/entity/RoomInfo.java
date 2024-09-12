package com.inspur.industrialinspection.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
/**
 * 机房信息
 * @author kliu
 * @date 2022/5/24 20:25
 */
@ApiModel("机房信息")
@ToString
@Data
public class RoomInfo {
    @ApiModelProperty("机房id")
    private long roomId;
    @Size(max = 128, message = "机房名称长度不能大于128")
    @NotBlank(message = "机房名称不能为空")
    @ApiModelProperty("机房名称")
    private String roomName;
    @Size(max = 255, message = "机房地址长度不能大于255")
    @ApiModelProperty("机房地址")
    private String roomAddr;
    @ApiModelProperty(value = "是否在用", hidden = true)
    private String inUse;
    @ApiModelProperty("园区id")
    private int parkId;
    @ApiModelProperty("楼栋id")
    private long buildingId;
    @ApiModelProperty("机器人id")
    private long robotId;
    @ApiModelProperty("缩略图")
    private String thumbnailUrl;
}