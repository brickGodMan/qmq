package com.qiancy.qmq.core;

/**
 * 功能简述：生产者
 *
 * @author qiancy
 * @create 2021/1/17
 * @since 1.0.0
 */
public class QmqProducer {

    private QmqBroker qmqBroker;

    private Qmq qmq;

    public QmqProducer(QmqBroker qmqBroker) {
        this.qmqBroker = qmqBroker;
    }

    /**
     * 发送消息方法
     *
     * @param topic
     * @param message
     * @return
     * @throws Exception
     */
    public boolean send(String topic, QmqMessage message) throws Exception {
        qmq = this.qmqBroker.getQmq(topic);
        if (null == qmq) {
            throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        }
        return qmq.send(message);
    }

    public boolean confirm() {
        return qmq.commit();
    }
}
