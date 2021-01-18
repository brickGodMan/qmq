import java.util.concurrent.atomic.AtomicInteger;

/**
 * 功能简述：
 *
 * @author qiancy
 * @create 2021/1/17
 * @since 1.0.0
 */
public class Test {

    public static void main(String[] args) {
        AtomicInteger writeOffset = new AtomicInteger();
        writeOffset.set(0);
        System.out.println(writeOffset.getAndAdd(1));
        System.out.println(writeOffset.getAndAdd(1));
        System.out.println(writeOffset.get());
    }
}
