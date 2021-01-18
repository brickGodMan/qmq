package com.qiancy.qmq.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * 功能简述：消息头-消息体
 *
 * @author qiancy
 * @create 2021/1/17
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class QmqMessage<T> {
    /**
     * 消息头
     */
    private HashMap<String,Object> headers;

    /**
     * 消息体
     */
    private T body;
}
