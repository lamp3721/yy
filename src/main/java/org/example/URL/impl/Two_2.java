package org.example.URL.impl;

import org.example.URL.YiYanApi;
import org.example.entity.Y;
import org.example.util.Http;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class Two_2 implements YiYanApi {
    public int id = 2;
    @Value("${api2}")
    String apiUrl;
    @Resource
    private Y y;


    @Override
    public Y conn() {
        y.clear(); // 清空y
        y.setUrId(id);
        y.setUrl(apiUrl);
        String body = Http.get(apiUrl);

        if (body.equals("404")) {
            //出错了
            y.setStatus(0);
            return y;
        }
        try {
            JSONObject jsonObject = new JSONObject(body);
            JSONObject data = jsonObject.getJSONObject("data");
            String content = data.getString("content");
            String origin = data.getString("origin");

            y.setStatus(1);
            y.setMsg(content);
            y.setAuthor(origin);
        } catch (Exception e) {
            y.setStatus(0);
        }
        return y;

    }


}
