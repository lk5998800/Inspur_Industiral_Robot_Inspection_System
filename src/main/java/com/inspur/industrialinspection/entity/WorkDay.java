package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * 工作日信息
 * @author kliu
 * @date 2022/7/27 15:20
 */
@ApiModel("工作日信息")
@ToString
@Data
public class WorkDay {
  private String dateStr;
  private String dateType;
}
