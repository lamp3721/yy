package org.example;


import org.example.config.JavaConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(JavaConfiguration.class);  //配置类

        //Eight_8 bean = applicationContext.getBean(Eight_8.class);
        //System.out.println(bean.conn());
        
    }
}