package org.example.service;


import org.example.URL.YiYanApi;
import org.example.pojo.Y;
import org.springframework.scheduling.annotation.Scheduled;
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
    
    @Scheduled(fixedDelayString = "#{T(java.lang.Math).round(T(java.lang.Math).random() * 5000 + 300000)}")
    public void execute() {
        while (true) { // 无限循环
            try {
                // 获取接口数据
                YiYanApi yiYanApi = all.getY();
                Y y = yiYanApi.conn();

                // 检查返回数据是否符合要求
                if (y.getStatus() == 0 || y.getMsg().length() == 0 || y.getMsg().length() > 60) {
                    if (y.getMsg().length() <= 60) {
                        recordFail(y); // 记录错误
                    }
                    // 网络错误或数据无效，等待 5 秒后重新尝试
                    Thread.sleep(5000);
                    continue; // 重新尝试
                }

                // 显示内容
                show.updateShow(y);

                // 成功记录
                record(y);

                // 成功后退出循环
                break;

            } catch (Exception e) {
                // 发生异常时记录日志并继续重试
                try {
                    Thread.sleep(5000); // 等待 5 秒后再重试
                } catch (InterruptedException ie) {
                }
            }
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


}
