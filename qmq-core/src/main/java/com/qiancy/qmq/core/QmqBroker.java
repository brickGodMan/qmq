package com.qiancy.qmq.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能简述：
 *
 * @author qiancy
 * @create 2021/1/17
 * @since 1.0.0
 */
public class QmqBroker {

    private static final int INIT_CAPACITY = 1000;

    private Map<String, Qmq> qmqMap = new ConcurrentHashMap<>(64);

    public void createTopic(String topic) {
        qmqMap.putIfAbsent(topic, new Qmq(topic, INIT_CAPACITY));
    }

    public Qmq getQmq(String topic) {
        return qmqMap.get(topic);
    }

    public QmqProducer getQmqProducer() {
        return new QmqProducer(this);
    }

    public QmqConsumer getQmqConsumer() {
        return new QmqConsumer(this);
    }
}
