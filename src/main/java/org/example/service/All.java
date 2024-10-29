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
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Autowired
    private Zero_0 zero_0;
    @Autowired
    private One_1 one_1;
    @Autowired
    private Two_2 two_2;
    @Autowired
    private Three_3 three_3;
    @Autowired
    private Four_4 four_4;
    @Autowired
    private Five_5 five_5;
    @Autowired
    private Six_6 six_6;
    @Autowired
    private Seven_7 seven_7;
    @Autowired
    private Eight_8 eight_8;
    @Autowired
    private Ten_10 ten_10;
    @Autowired
    private Eleven_11 eleven_11;
    @Autowired
    private Twelve_12 twelve_12 ;
    @Autowired
    private Thirteen_13 thirteen_13;
    @Autowired
    private Fourteen_14 fourteen_14 ;
    @Autowired
    private Fifteen_15 fifteen_15;
    @Autowired
    private Sixteen_16 sixteen_16;
    @Autowired
    private Seventeen_17 seventeen_17;
    @Autowired
    private Eighteen_18 eighteen_18;
    @Autowired
    private Nineteen_19 nineteen_19;
    @Autowired
    private Twenty_20 twenty_20;
    
    
    @PostConstruct
    public boolean setAll(){
        ys.add(zero_0);
        ys.add(one_1);
        ys.add(two_2);
        ys.add(three_3);
        ys.add(four_4);
        ys.add(five_5);
        ys.add(six_6);
        ys.add(seven_7);
        ys.add(eight_8);
        ys.add(ten_10);
        ys.add(eleven_11);
        ys.add(twelve_12);
        ys.add(thirteen_13);
        ys.add(fourteen_14);
        ys.add(fifteen_15);
        ys.add(sixteen_16);
        ys.add(seventeen_17);
        ys.add(eighteen_18);
        ys.add(nineteen_19);
        ys.add(twenty_20);
        
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
