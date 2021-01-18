package com.qiancy.qmq.demo;

import lombok.Builder;
import lombok.Data;

/**
 * 功能简述：
 *
 * @author qiancy
 * @create 2021/1/17
 * @since 1.0.0
 */
@Data
@Builder
public class Person {

    private String name;

    private String age;
}
