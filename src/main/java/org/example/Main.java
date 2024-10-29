package org.example;

import org.example.URL.impl.*;
import org.example.service.*;
import org.example.config.JavaConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(JavaConfiguration.class);  //配置类
        //Eighteen_18 bean = applicationContext.getBean(Eighteen_18.class);
        //System.out.println(bean.conn());

    }
}