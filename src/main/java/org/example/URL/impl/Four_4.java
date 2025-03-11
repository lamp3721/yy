package org.example.URL.impl;

import org.example.URL.YiYanApi;
import org.example.entity.Y;
import org.example.pool.YPool;
import org.example.util.Http;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class Four_4 implements YiYanApi {
    int id = 4;
    @Value("${api4}")
    String apiUrl;
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


        y.setStatus(1);
        y.setMsg(body);

        return y;

    }


}
