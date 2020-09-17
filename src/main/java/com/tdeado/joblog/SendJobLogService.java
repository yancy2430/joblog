package com.tdeado.joblog;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Service
public class SendJobLogService {
    @Autowired
    JavaMailSender mailSender;

    public boolean send(String complete,String issue,String tomorrow) throws MessagingException {
        //复杂邮件
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage,true);
        messageHelper.setFrom("2850755733@qq.com");
        messageHelper.setTo("yancy@tdeado.com");
        messageHelper.setSubject(LocalDate.now().toString()+".杨哲");

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
            configuration.setClassForTemplateLoading(this.getClass(), "/templates");

            //输出文档路径及名称
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //以utf-8的编码读取ftl文件
            Template template = configuration.getTemplate("joblog.ftl", "utf-8");
            Writer out = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream, "utf-8"), 10240);
            template.process(dataMap, out);
            out.close();
            messageHelper.addAttachment(LocalDate.now().toString()+".杨哲.docx",new ByteArrayResource(byteArrayOutputStream.toByteArray()));
            byteArrayOutputStream.close();
            StringWriter stringWriter = new StringWriter();
            template = configuration.getTemplate("joblog_html.ftl", "utf-8");
            Writer outHtml = new BufferedWriter(stringWriter, 10240);
            template.process(dataMap, outHtml);
            stringWriter.flush();
            stringWriter.close();
            messageHelper.setText(stringWriter.toString(),true);

            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }
}
