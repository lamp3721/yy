package org.example.URL.impl;


import org.example.URL.YiYanApi;
import org.example.entity.Y;
import org.example.util.Http;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class Sixteen_16 implements YiYanApi {

    public int id = 16;

    @Value("${api16}")
    public String apiUrl;

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
            y.setStatus(1);
            y.setMsg(body);
        } catch (Exception e) {
            y.setStatus(0);
        }

        return y;
    }
}
