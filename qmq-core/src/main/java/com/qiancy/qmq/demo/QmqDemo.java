package com.qiancy.qmq.demo;

import com.qiancy.qmq.core.QmqBroker;
import com.qiancy.qmq.core.QmqConsumer;
import com.qiancy.qmq.core.QmqMessage;
import com.qiancy.qmq.core.QmqProducer;

import java.util.List;
import java.util.Random;

/**
 * 功能简述：
 *
 * @author qiancy
 * @create 2021/1/17
 * @since 1.0.0
 */
public class QmqDemo {

    public static void main(String[] args) throws Exception {
        String topic = "qiancy";
        QmqBroker broker = new QmqBroker();

        broker.createTopic(topic);

        QmqConsumer consumer = broker.getQmqConsumer();
        consumer.subscribe(topic);
        final boolean[] flag = new boolean[1];
        flag[0] = true;
        new Thread(() -> {
            while (flag[0]) {
                List<QmqMessage<Person>> message = consumer.poll();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (null != message) {
                    for (QmqMessage<Person> var : message) {
                        System.out.println(var.getBody());
                    }
                }
            }
            System.out.println("程序退出。");
        }).start();

        QmqProducer producer = broker.getQmqProducer();
        for (int i = 0; i < 1000; i++) {
            Person person = Person.builder().age(i + "").name("qiancy").build();
            producer.send(topic, new QmqMessage(null, person));
        }
        Thread.sleep(500);
        producer.confirm();
        System.out.println("点击任何键，发送一条消息；点击q或e，退出程序。");
        while (true) {
            char c = (char) System.in.read();
            if (c > 20) {
                System.out.println(c);
                producer.send(topic, new QmqMessage(null, Person.builder().age("10").name(String.valueOf(c)).build()));
            }
            if (c == 'q' || c == 'e') break;
        }

        flag[0] = false;
    }
}
