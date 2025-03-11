package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.URL.YiYanApi;
import org.example.entity.Y;
import org.example.event.YEventPublisher;
import org.example.pool.YPool;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class YTaskService {

    @Resource
    private All all;  // 封装 API 获取逻辑

    @Resource
    private YEventPublisher yEventPublisher;  // 发布事件的封装类

    /**
     * 主任务执行方法：获取数据、校验并发布事件
     */
    public void executeTask() {
        while (true) {
            try {
                YiYanApi api = all.getY();         // 获取 API 接口实例
                Y y = api.conn();                  // 调用接口获取数据

                if (isValid(y)) {
                    yEventPublisher.publish(y);   // 发布事件
                    break;
                }

                log.info("无效返回数据: {}", y);
                handleInvalidData(y);
                yEventPublisher.publish(y);
                YPool.returnY(y);  // 归还连接
                Thread.sleep(10000); // 10 秒后重试


            } catch (Exception e) {
                log.error("任务执行异常：", e);
                try {
                    Thread.sleep(5000); // 异常休眠 5 秒再重试
                } catch (InterruptedException ie) {
                    log.warn("线程中断", ie);
                }
            }
        }
    }

    /**
     * 数据校验方法
     */
    private boolean isValid(Y y) {
        return y.getStatus() != 0
                && y.getMsg() != null
                && y.getMsg().length() > 0
                && y.getMsg().length() <= 60;
    }

    /**
     * 处理非法/异常数据
     */
    private void handleInvalidData(Y y) {
        y.setMsg(y.getUrId() + ": 出错了！");
        y.setStatus(0);
    }
}
