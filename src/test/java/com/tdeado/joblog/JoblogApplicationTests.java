package com.tdeado.joblog;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class JoblogApplicationTests {

    @Test
    void contextLoads() {
        Map<String,Object> dataMap = new HashMap<>();
        try {
            dataMap.put("complete", "修复一些bug\n修复前端问题");
            dataMap.put("issue", "无");
            dataMap.put("tomorrow", "新框架数据中心开发");
            Configuration configuration = new Configuration(new Version("2.3.0"));
            configuration.setDefaultEncoding("utf-8");

            /**
             * 以下是两种指定ftl文件所在目录路径的方式，注意这两种方式都是
             * 指定ftl文件所在目录的路径，而不是ftl文件的路径
             */
            //指定路径的第一种方式（根据某个类的相对路径指定）
//                configuration.setClassForTemplateLoading(this.getClass(), "");

            //指定路径的第二种方式，我的路径是C：/a.ftl
            configuration.setDirectoryForTemplateLoading(new File("/Users/yangzhe/joblog/src/main/resources/templates"));

            //输出文档路径及名称
            File outFile = new File("/Users/yangzhe/joblog/src/main/resources/joblog.docx");

            //以utf-8的编码读取ftl文件
            Template template = configuration.getTemplate("joblog.ftl", "utf-8");
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"), 10240);
            template.process(dataMap, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
