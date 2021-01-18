package com.qiancy.qmq.core;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 功能简述：qmq消费者
 *
 * @author qiancy
 * @create 2021/1/17
 * @since 1.0.0
 */
public class QmqConsumer<T> {

    private QmqBroker broker;

    private Qmq qmq;

    private AtomicInteger offset = new AtomicInteger();

    public QmqConsumer(QmqBroker broker) {
        this.broker = broker;
        offset.set(0);
    }

    /**
     * 订阅主题
     * @param topic
     */
    public void subscribe(String topic) {
        this.qmq = this.broker.getQmq(topic);
        if (null == qmq) {
            throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        }
    }

    public List<QmqMessage<T>> poll() {
        List<QmqMessage<T>> result = qmq.poll(offset.get());
        if(Objects.nonNull(result)) {
            offset.addAndGet(result.size());
            return result;
        }
        return null;
    }
}
