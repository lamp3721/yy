package org.example.URL.impl;

import org.example.URL.YiYanApi;
import org.example.entity.Y;
import org.example.util.Http;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class One_1 implements YiYanApi {
    int id = 1;
    @Value("${api1}")
    String apiUrl;
    @Resource
    private Y y;


    public Y conn() {

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
            String hitokoto = jsonObject.getString("hitokoto");
            String creator = jsonObject.getString("creator");

            y.setStatus(1);
            y.setMsg(hitokoto);
            y.setAuthor(creator);
        } catch (Exception e) {
            y.setStatus(0);
        }
        return y;

    }

}