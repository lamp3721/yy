package org.example;

import org.example.URL.impl.Five_5;
import org.example.service.*;
import org.example.URL.impl.Eight_8;
import org.example.URL.impl.Eleven_11;
import org.example.URL.impl.Four_4;
import org.example.URL.impl.Six_6;
import org.example.URL.impl.Ten_10;
import org.example.URL.impl.Zero_0;
import org.example.config.JavaConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(JavaConfiguration.class);  //配置类

        //Five_5 bean = applicationContext.getBean(Five_5.class);
        //System.out.println(bean.conn());

    }
}