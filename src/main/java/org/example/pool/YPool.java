package org.example.pool;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.example.entity.Y;

/**
 * Y对象池管理类，基于Apache Commons Pool实现
 */
public class YPool {
    private static final GenericObjectPool<Y> pool;

    static {
        GenericObjectPoolConfig<Y> config = new GenericObjectPoolConfig<>();
        // 池配置参数
        config.setMaxTotal(30); // 最大活跃对象数
        config.setMinIdle(1);   // 最小空闲对象数
        config.setMaxIdle(3);  // 最大空闲对象数
        config.setBlockWhenExhausted(true); // 资源耗尽时是否阻塞
        config.setMaxWaitMillis(5000);       // 获取对象最大等待时间（毫秒）

        // 初始化对象池
        pool = new GenericObjectPool<>(new YFactory(), config);
    }

    /**
     * 从池中借用一个Y对象
     * @return 可用的Y对象实例
     * @throws Exception 当获取对象超时或失败时抛出
     */
    public static Y borrowY() throws Exception {
        return pool.borrowObject();
    }

    /**
     * 归还Y对象到池中
     * @param y 要归还的Y对象实例
     */
    public static void returnY(Y y) {
        pool.returnObject(y);
    }
}
