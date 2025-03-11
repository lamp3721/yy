package org.example.URL.impl;

import org.example.URL.YiYanApi;
import org.example.entity.Y;
import org.example.pool.YPool;
import org.example.util.Http;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class Zero_0 implements YiYanApi {

    public int id = 0;

    @Value("${api0}")
    public String apiUrl;

    @Resource
    private Y y;


    public Y conn() throws Exception {
Y y = YPool.borrowY();
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
            String source = data.getString("source");

            y.setStatus(1);
            y.setMsg(hitokoto);
            y.setAuthor(source);
        } catch (Exception e) {
            y.setStatus(0);
        }
        return y;

    }


}
