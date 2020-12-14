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
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Service
public class SendJobLogService {
    @Autowired
    JavaMailSender mailSender;
    @Autowired
    ConfigProperties configProperties;

    public boolean send(String complete,String issue,String tomorrow) throws MessagingException, UnsupportedEncodingException {
        //复杂邮件
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage,true);
        messageHelper.setFrom(configProperties.getMailFrom());
        for (String s : configProperties.getMailTo().split(",")) {
            messageHelper.addTo(s);
        }
        String name=new String(configProperties.getName().getBytes("ISO-8859-1"), "utf-8");
        messageHelper.setSubject(LocalDate.now().toString()+"."+name);
        Map<String,Object> dataMap = new HashMap<>();
        try {
            dataMap.put("complete", complete);
            dataMap.put("issue", issue);
            dataMap.put("tomorrow", tomorrow);
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
            messageHelper.addAttachment(LocalDate.now().toString()+"."+name+".doc",new ByteArrayResource(byteArrayOutputStream.toByteArray()));
            byteArrayOutputStream.close();
            StringWriter stringWriter = new StringWriter();
            template = configuration.getTemplate("joblog_html.ftl", "utf-8");
            Writer outHtml = new BufferedWriter(stringWriter, 10240);
            template.process(dataMap, outHtml);
            stringWriter.flush();
            stringWriter.close();
            messageHelper.setText(stringWriter.toString().replace("<w:br/>","<br/>"),true);

            mailSender.send(mimeMessage);
            System.err.println("日志发送成功 "+LocalDate.now().toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    public static void main(String[] args) {
        try {
            System.err.println(MimeUtility.encodeText("杨哲"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
