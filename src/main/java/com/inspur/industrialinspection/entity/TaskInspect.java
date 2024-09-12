package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 巡检任务信息
 * @author wangzhaodi
 * @date 2022/11/16 14：02
 */
@ApiModel("手机巡检任务信息")
@ToString
@Data
public class TaskInspect {
    @ApiModelProperty("巡检任务id")
    private long taskInspectId;
    @ApiModelProperty("机房名称")
    private String roomName;
    @ApiModelProperty("开始时间")
    private String startTime;
    @ApiModelProperty("结束时间")
    private String endTime;
    @ApiModelProperty("任务状态")
    private int status;
}
