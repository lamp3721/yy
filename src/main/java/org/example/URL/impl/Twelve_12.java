package org.example.URL.impl;

import org.example.URL.YiYanApi;
import org.example.pojo.Y;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


@Component
@PropertySource("classpath:url.properties")
public class Twelve_12 implements YiYanApi {
    public int id = 12;
    @Value("${api12}")
    public String apiUrl ;
    @Resource
    private Y y;


    public Y conn() {
        y.clear(); // 清空y
        y.setUrId(id);
        y.setUrl(apiUrl);
        try {
            URL url = new URL(apiUrl);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为GET
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);



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
                // 解析 JSON 数据
                JSONObject jsonResponse = new JSONObject(""+response);
                String hitokoto = jsonResponse.getString("pyq");//内容



                System.out.println(id + ":" + apiUrl + ":" + hitokoto );

                y.setStatus(1);
                y.setMsg(hitokoto);

                return y;
            }
        } catch (Exception e) {
        }
        y.setStatus(0);
        return y;

    }
    



}