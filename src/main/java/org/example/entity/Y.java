package org.example.entity;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Data
@Scope("prototype")
public class Y {
    private int  status; //状态码
    private int urId;
    private String url;
    private String msg;
    private String author;
}
