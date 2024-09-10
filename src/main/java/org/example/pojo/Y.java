package org.example.pojo;

import org.springframework.stereotype.Component;

@Component
public class Y {
    private int  status; //状态码
    private int urId;
    private String msg;
    private String author;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getUrId() {
        return urId;
    }

    public void setUrId(int urId) {
        this.urId = urId;
    }

    //清除bean中数据
    public void clear() {
        this.status = 0;
        this.urId = 100;
        this.msg = "";
        this.author = "";
    }

    @Override
    public String toString() {
        return "Y{" +
                "author='" + author + '\'' +
                ", status=" + status +
                ", urId=" + urId +
                ", msg='" + msg + '\'' +
                '}';
    }
}
