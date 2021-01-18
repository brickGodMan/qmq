### 自定义实现MQ


**1.思考和设计自定义MQ，写代码实现其中至少一个功能点。**

实现自定义queue，实现消息确认和消费offset。

- 自定义内存message数组模拟queue

- 使用指针记录当前消息写入位置

- 对于每个命名消费者，用指针记录消费位置

> 设计思路
>
> 1. 所有消费者都是读取同一个数组，这里在broker中使用一个AtomicInteger成员变量来保存当前消息的写入位置（writerOffset）。为了实现消息确认必须在加入一个AtomicInteger成员变量用来保存当前可读消息位置（CurrentOffset），等到生产者确认消息写入在把writerOffset的值赋值给currenOffset。
> 2. 给每个消费者client增加一个offset属性，每次读取queue中消息时都从自己offset属性位置开始。如果消费者client中的offset异常默认从零开始消费消息。





*草稿：*

1. *qmq 里面存放主题topic ，队列大小capacity，以及队列queue，并提供放入消息方法（往数组里面塞值）和获取消息方法（获取数组里面的值）。后续实现消息确认需要增加两个属性，当前可消费的位置，以及消息写入的位置*
2. *qBroker 里面是一个qmq容器，使用map存放不同topic 的 queue，并提供获取每个主题qmq的方法，获取生产者的方法以及获取消费者的方法。*
3. *qmqProducer 里面存放一个broker实例，可以获取对应主题的qmq，可以通过qmq方法在相应队列里面放入消息*
4. *qmqConsumer里面存放一个broker实例，可以获取对应主题的qmq，可以通过qmq方法在相应队列里面获取消息。后续实现消费offset需要增加成员变量。*



**关键代码：**

```java
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
```



**consumer 增加offset 机制保证每次消费都从最新位置消费：**

```java
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
```

**producer发送消息后必须调用confirm方法，不然消费者无法消费到最新消息：**

```java
QmqProducer producer = broker.getQmqProducer();
        for (int i = 0; i < 1000; i++) {
            Person person = Person.builder().age(i + "").name("qiancy").build();
            producer.send(topic, new QmqMessage(null, person));
        }
        Thread.sleep(500);
		//send之后必须要调用确认方法
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
```

