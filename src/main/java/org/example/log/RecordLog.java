package org.example.log;

import com.google.common.eventbus.Subscribe;
import org.example.entity.Y;
import org.example.event.YEvent;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


@Component
public class RecordLog {
    //成功记录
    // 监听事件
    @Subscribe
    private void record(YEvent yEvent) throws IOException {
        Y y = yEvent.getY();
        if (y.getStatus() != 1) {
            return;
        }
        File yYText = new File("一言.txt");

        // 指定字符编码为 UTF-8
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(yYText, true), "UTF-8");
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        if ( y.getAuthor() != null && y.getAuthor().length() > 0) {
            bufferedWriter.write("*" + ":" + y.getMsg() + " -- " + y.getAuthor() + "\n");
        } else {
            bufferedWriter.write("*" + ":" + y.getMsg() + "\n");
        }

        bufferedWriter.flush();
        bufferedWriter.close();  // 关闭资源以确保数据写入文件
    }
}
