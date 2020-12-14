package com.tdeado.joblog;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <pre>
 *  微信支付属性配置类
 * Created by Binary Wang on 2019/4/17.
 * </pre>
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Data
@Component
@ConfigurationProperties(prefix = "joblog")
public class ConfigProperties {
  String didaUserName;
  String didaPassWord;
  String mailFrom;
  String mailTo;
  String name;
  String projectId;
  String undoneColumnId;
  String issueColumnId;
}
