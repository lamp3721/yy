package org.example.URL.impl;

import org.example.URL.YiYanApi;
import org.example.entity.Y;
import org.example.util.Http;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class Ten_10 implements YiYanApi {
    public int id = 10;
    @Value("${api10}")
    public String apiUrl;
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

        y.setStatus(1);
        y.setMsg(body);

        return y;
    }
}
