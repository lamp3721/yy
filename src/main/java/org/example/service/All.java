package org.example.service;

import org.example.URL.YiYanApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class All {

    @Autowired
    private ApplicationContext applicationContext;

    ArrayList<YiYanApi> ys = new ArrayList<>();

    SecureRandom sr = new SecureRandom();
    
    @PostConstruct
    public void setAll(){
        // 获取所有 YiYanApi 接口的实现类，返回的是一个 Map<String, YiYanApi>
        Collection<YiYanApi> yiYanApiBeans = applicationContext.getBeansOfType(YiYanApi.class).values();

        // 将 Collection 转换为数组
        YiYanApi[] yiYanApiArray = yiYanApiBeans.toArray(new YiYanApi[0]);

        // 输出数组中的每个实现类对象的 say() 方法
        for (YiYanApi yiYanApi : yiYanApiArray) {
            yiYanApi.conn();
            ys.add(yiYanApi);
        }
        System.out.println("------------测试完毕------------");
    }
    
    //随机返回一个YiYan接口的实现类
    public YiYanApi getY(){
        //生成随机数
        int i = sr.nextInt(ys.size());
        return ys.get(i);
    }
}
