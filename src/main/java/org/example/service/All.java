package org.example.service;


import lombok.extern.slf4j.Slf4j;
import org.example.URL.YiYanApi;
import org.example.URL.impl.*;
import org.example.entity.Y;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

// 获取所有实现类
@Service
@Slf4j
public class All {

    @Autowired
    private ApplicationContext applicationContext;

    ArrayList<YiYanApi> ys = new ArrayList<>();

    @PostConstruct
    public boolean setAll(){

        // 获取所有实现类Bean
        ys.addAll(applicationContext.getBeansOfType(YiYanApi.class).values());
        
        // 输出数组中的每个实现类对象的 say() 方法
        for (YiYanApi yiYanApi : ys) {
            //测试连接
            Y y = yiYanApi.conn();
            if(y.getStatus() == 0){
                log.info(y.toString());
                continue;
            }
            System.out.println(y.getUrId()+" 成功: "+y.getMsg());
        }
        System.out.println("------------测试完毕------------"+ys.size());
        return true;
    }
    
    //随机返回一个YiYan接口的实现类
    public YiYanApi getY() {
        int index = ThreadLocalRandom.current().nextInt(ys.size());
        return ys.get(index);
    }
}
