package org.example;


import org.example.URL.YiYan;
import org.example.URL.impl.Eight_8;
import org.example.config.JavaConfiguration;
import org.example.service.All;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(JavaConfiguration.class);  //配置类

        //Eight_8 bean = applicationContext.getBean(Eight_8.class);
        //System.out.println(bean.conn());
        All bean = applicationContext.getBean(All.class);



    }
}