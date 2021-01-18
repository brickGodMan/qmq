package com.qiancy.qmq.core;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 功能简述：
 *
 * @author qiancy
 * @create 2021/1/17
 * @since 1.0.0
 */

public class Qmq<T> {

    private String topic;

    private Integer capacity;

    private Object[] queue;

    private int size;

    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 当前写入位置
     */
    private AtomicInteger writeOffset = new AtomicInteger();

    /**
     * 当前可读取位置
     */
    private AtomicInteger currentOffset = new AtomicInteger();

    public Qmq(String topic, Integer capacity) {
        this.topic = topic;
        this.capacity = capacity;
        this.queue = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
        size = queue.length;
        writeOffset.set(0);
        currentOffset.set(0);
    }

    /**
     * 发送方法
     *
     * @param message
     * @return
     * @throws Exception
     */
    public boolean send(QmqMessage message) {
        ensureCapacityInternal(size + 1);
        writeOffset.set(size + 1);
        queue[size++] = message;
        return true;
    }

    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(queue, minCapacity));
    }

    private void ensureExplicitCapacity(int calculateCapacity) {
        if (calculateCapacity - queue.length > 0) {
            grow(calculateCapacity);
        }
    }

    private void grow(int minCapacity) {
        int oldCapacity = queue.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity - capacity > 0) {
            newCapacity = capacity;
            System.out.println(String.format("this topic: %s is oversize! and the laster size will be override", this.topic));
        }
        queue = Arrays.copyOf(queue, newCapacity);
    }

    /**
     * 接收方法
     *
     * @return
     */
    public List<QmqMessage<T>> poll(int offerSet) {
        List<QmqMessage<T>> result = Lists.newArrayList();
        if (Objects.nonNull(queue) && queue.length > 0) {
            for (int i = offerSet; i < currentOffset.get(); i++) {
                result.add((QmqMessage<T>) queue[i]);
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * 确认生产者消息可以让消费者消费
     *
     * @return
     */
    public boolean commit() {
        currentOffset.set(writeOffset.get());
        return currentOffset.get() > 0;
    }


    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }
}
