package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 阈值信息
 * @author kliu
 * @date 2022/5/24 20:27
 */
@ApiModel("阈值信息")
@ToString
@Data
public class Threshold {
    @ApiModelProperty("预警级别 预警 一般 严重 危机")
    private String level;
    @ApiModelProperty("预警下限")
    private String thresholdLl;
    @ApiModelProperty("预警上限")
    private String thresholdUl;
}
