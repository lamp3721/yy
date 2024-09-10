package org.example.service;

import org.example.URL.YiYan;
import org.example.pojo.Y;
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

    ArrayList<YiYan> ys = new ArrayList<>();

    SecureRandom sr = new SecureRandom();
    
    @PostConstruct
    public void getY(){
        // 获取所有 YiYan 接口的实现类，返回的是一个 Map<String, YiYan>
        Collection<YiYan> yiYanBeans = applicationContext.getBeansOfType(YiYan.class).values();

        // 将 Collection 转换为数组
        YiYan[] yiYanArray = yiYanBeans.toArray(new YiYan[0]);

        // 输出数组中的每个实现类对象的 say() 方法
        for (YiYan yiYan : yiYanArray) {
            yiYan.conn();
            ys.add(yiYan);
        }
    }
    
    //随机返回一个YiYan接口的实现类
    public YiYan get(){
        int i = (int) Math.random() * ys.size();
        return ys.get(i);
    }
}
