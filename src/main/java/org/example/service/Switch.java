package org.example.service;


import org.example.URL.YiYanApi;
import org.example.pojo.Y;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

@Service
public class Switch {
    
    @Resource
    All all;
    
    @Resource
    Show show;
    
    
    private File yYText;

    @PostConstruct
    public void init() throws InterruptedException, IOException {
        
        while (true){
            YiYanApi yiYanApi = all.getY();
            Y y = yiYanApi.conn();
            if(y.getStatus() == 0 || y.getMsg().length() == 0 || y.getMsg().length() > 60){
                //网络错误
                recordFail(y);//记录错误
                Thread.sleep(5000);
                continue;
            }
            
            //显示
            show.show(y);
            
            //记录
            record(y);
            
            //休眠随机时间
            Thread.sleep(generateRandomNumber());
        }
        
        
        
    }

    //成功记录
    private void record(Y y) throws IOException {
        yYText = new File("一言.txt");
        FileWriter fileWriter = new FileWriter(yYText, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        if(y.getAuthor().length() > 0){
            bufferedWriter.write("*"+":" + y.getMsg() +" -- "+ y.getAuthor() + "\n");
        }else{
            bufferedWriter.write("*"+":" + y.getMsg() + "\n");
        }
        bufferedWriter.flush();
    }
    //失败记录
    private void recordFail(Y y) throws IOException {
        yYText = new File("err.txt");
        FileWriter fileWriter = new FileWriter(yYText, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(y.getUrId()+":"+y.getUrl()+"\n");
    }

    //随机时间
    public long generateRandomNumber() {
        Random random = new Random();
        int lowerBound = 5 * 1000 * 60;
        int upperBound = 30 * 1000 * 60;
        // 生成一个介于 lowerBound（包含）和 upperBound（不包含）之间的随机数
        int randomNumber = random.nextInt(upperBound - lowerBound) + lowerBound;
        System.out.println(randomNumber / 1000/60+"分钟");
        return randomNumber;
    }
}
