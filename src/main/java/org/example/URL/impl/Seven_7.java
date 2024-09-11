package org.example.URL.impl;

import org.example.URL.YiYanApi;
import org.example.pojo.Y;
import org.example.util.Http;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class Seven_7 implements YiYanApi {

    int id = 7;
    @Value("${api7}")
    String apiUrl;
    @Resource
    private Y y;


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
            String hitokoto = data.getString("hitokoto");
            String from = data.getString("from");
            y.setStatus(1);
            y.setMsg(hitokoto);
            y.setAuthor(from);
        } catch (Exception e) {
            y.setStatus(0);
        }
        return y;

    }

}
