package org.example.pool;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.example.entity.Y;

public class YFactory implements PooledObjectFactory<Y> {
    @Override
    public PooledObject<Y> makeObject() {
        return new DefaultPooledObject<>(new Y());
    }

    @Override
    public void destroyObject(PooledObject<Y> pooledObject) {
        // 可以清理资源
    }

    @Override
    public boolean validateObject(PooledObject<Y> pooledObject) {
        return true;
    }

    @Override
    public void activateObject(PooledObject<Y> pooledObject) {
        // 初始化或重置对象状态
    }

    @Override
    public void passivateObject(PooledObject<Y> pooledObject) {
        // 重置对象状态以供下次使用
        Y y = pooledObject.getObject();
        y.clear(); // 自定义清理方法
    }
}
