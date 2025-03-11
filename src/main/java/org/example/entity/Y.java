package org.example.entity;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Data
public class Y {
    private int  status; //状态码
    private int urId;
    private String url;
    private String msg;
    private String author;

    //清理bean
    public void clear(){
        status = 0;
        urId = 100;
        url = "";
        msg = "";
        author = "";
    }
}
