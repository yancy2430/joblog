package com.tdeado.joblog;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
@EnableScheduling   // 2.开启定时任务
@SpringBootApplication
public class JoblogApplication{

    public static void main(String[] args) {
        SpringApplication.run(JoblogApplication.class, args);
    }
    @Bean
    public RestTemplate init(){
        return new RestTemplateBuilder().build();
    }
    @Bean
    public OkHttpClient okhhttp3(){
        return new OkHttpClient();
    }
    @Autowired
    OkHttpClient client;

    @Autowired
    SendJobLogService sendJobLogService;
    @Autowired
    ConfigProperties configProperties;
    @Scheduled(cron = "0 0 19 * * ?")
    public void sendMail() throws IOException, MessagingException {

        Response response = client.newBuilder().build().newCall(new Request.Builder()
                .url("https://api.dida365.com/api/v2/user/signon?wc=true&remember=true")
                .post(RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), "{\"password\":\""+configProperties.getDidaPassWord()+"\",\"username\":\""+configProperties.getDidaUserName()+"\"}"))
                .build()
        ).execute();
        JsonElement json = JsonParser.parseString(response.body().string());
        response.close();
        String token = json.getAsJsonObject().get("token").getAsString();

        String end = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Response resp = client.newBuilder().build().newCall(new Request.Builder()
                .url("https://api.dida365.com/api/v2/project/"+configProperties.getProjectId()+"/completed/?from=" + LocalDate.now() + " 00:00:00&to=" + end + "&limit=50")
                .addHeader("cookie","t="+token+";")
                .build()).execute();
        JsonElement jsonElement = JsonParser.parseString(resp.body().string()).getAsJsonArray();
        resp.close();
        StringBuffer complete = new StringBuffer();
        for (JsonElement element : jsonElement.getAsJsonArray()) {
            complete.append(element.getAsJsonObject().get("title").getAsString()+"<w:br/>");
        }
        if (complete.length()==0) {
            System.err.println("今天不发送"+LocalDate.now());
            return;
        }

        Response res = client.newBuilder().build().newCall(new Request.Builder()
                .url("https://api.dida365.com/api/v2/batch/check/0")
                .addHeader("cookie","t="+token+";")
                .build()).execute();
        JsonObject element = JsonParser.parseString(res.body().string()).getAsJsonObject();
        res.close();
        StringBuffer issue = new StringBuffer();
        StringBuffer tomorrow = new StringBuffer();
        for (JsonElement jsonElement1 : element.get("syncTaskBean").getAsJsonObject().get("update").getAsJsonArray()) {
            if (jsonElement1.getAsJsonObject().get("projectId").getAsString().equals(configProperties.getProjectId())){//项目ID
                if (jsonElement1.getAsJsonObject().get("columnId").getAsString().equals(configProperties.getUndoneColumnId())) {//未完成
                    tomorrow.append(jsonElement1.getAsJsonObject().get("title").getAsString()+"<w:br/>");
                }else if (jsonElement1.getAsJsonObject().get("columnId").getAsString().equals(configProperties.getIssueColumnId())){
                    issue.append(jsonElement1.getAsJsonObject().get("title").getAsString()+"<w:br/>");
                }
            }
        }
        sendJobLogService.send(complete.toString(),issue.toString(),tomorrow.toString());

    }

}
