package org.example.service;


import lombok.extern.slf4j.Slf4j;
import org.example.URL.YiYanApi;
import org.example.pojo.Y;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


// 定时刷新
@Service
@Slf4j
public class Switch {
    
    @Resource
    All all;
    
    @Resource
    Show show;
    
    private File yYText;
    
    @Scheduled(fixedDelayString = "#{T(java.lang.Math).round(T(java.lang.Math).random() * 5000 + 300000)}")
    public void execute() {
        while (true) { // 无限循环
            System.gc();
            try {
                // 获取接口数据
                YiYanApi yiYanApi = all.getY();
                Y y = yiYanApi.conn();
                
                // 检查返回数据是否符合要求
                if (y.getStatus() == 0 || y.getMsg().length() == 0 || y.getMsg().length() > 60) {
                    if (y.getMsg().length() <= 60) {
                        log.info(y.toString());
                    }
                    y.setMsg(y.getUrId()+":出错了！");
                    y.setStatus(0);
                    show.updateShow(y);
                    // 网络错误或数据无效，等待 10 秒后重新尝试
                    Thread.sleep(10000);
                    continue; // 重新尝试
                }

                // 显示内容
                show.updateShow(y);

                // 成功持久记录
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
        File yYText = new File("一言.txt");

        // 指定字符编码为 UTF-8
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(yYText, true), "UTF-8");
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        if (y.getAuthor().length() > 0) {
            bufferedWriter.write("*" + ":" + y.getMsg() + " -- " + y.getAuthor() + "\n");
        } else {
            bufferedWriter.write("*" + ":" + y.getMsg() + "\n");
        }

        bufferedWriter.flush();
        bufferedWriter.close();  // 关闭资源以确保数据写入文件
    }

}
