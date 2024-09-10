package org.example.URL.impl;

import org.example.URL.YiYanApi;
import org.example.pojo.Y;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
@PropertySource("classpath:url.properties")
public class Three_3 implements YiYanApi {
    private int id = 3;
    
    @Value("${api3}")
    String apiUrl ;
    @Resource
    private Y y;



    public static void main(String[] args) {
        Three_3 three3 = new Three_3();
        three3.conn();
    }

    @Override
    public Y conn() {
        y.clear(); // 清空y
        y.setUrId(id);
        y.setUrl(apiUrl);
        try {
            URL url = new URL(apiUrl);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            

            // 设置请求方法为 
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);

            // 设置请求参数


            // 获取响应状态码
            int responseCode = connection.getResponseCode();


            // 如果响应状态码为 200，表示请求成功
            if (responseCode == 200) {
                // 读取响应数据
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                //关闭流
                reader.close();
                //关闭连接
                connection.disconnect();

                String s = removeHtmlTags("" + response);
                System.out.println(id + ":" + apiUrl + ":" + s);

                y.setStatus(1);
                y.setMsg(s);

                return y;
            }
        } catch (Exception e) {
        }
        y.setStatus(0);
        return y;
    }


    public String removeHtmlTags(String htmlString) {
        Pattern pattern = Pattern.compile("<[^>]*>");
        Matcher matcher = pattern.matcher(htmlString);
        return matcher.replaceAll("");
    }




}
