package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * code配置
 * @author kliu
 * @date 2022/9/7 11:00
 */
@ApiModel("代码配置")
@ToString
@Data
public class CodeConfig {
  private String code;
  private String value;
  private String content;
}
