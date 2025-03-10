package org.example.URL.impl;

import org.example.URL.YiYanApi;
import org.example.entity.Y;
import org.example.util.Http;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class Twenty_20 implements YiYanApi {
    public int id = 20;

    @Value("${api20}")
    private String apiUrl;

    @Resource
    private Y y;



    @Override
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
            String msg = jsonObject.getString("data");

            y.setStatus(1);
            y.setMsg(msg);
        } catch (Exception e) {
            y.setStatus(0);
        }
        return y;

    }
}
