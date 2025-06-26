package singleton.simple;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SingletonTest {

  @DisplayName("일반적인 경우 싱글톤 객체 사용")
  @Test
  void test() {
    Singleton instance1 = Singleton.getInstance();
    Singleton instance2 = Singleton.getInstance();
    assertEquals(instance1, instance2);
  }

  @DisplayName("멀티 스레드 환경에서 싱글톤 파괴")
  @Test
  void testMultiThread() throws InterruptedException {
    for (int i = 0; i < 10000; ++i) {
      AtomicReference<Singleton> instance1 = new AtomicReference<>();
      AtomicReference<Singleton> instance2 = new AtomicReference<>();

      CountDownLatch startLatch = new CountDownLatch(1);
      CountDownLatch endLatch = new CountDownLatch(2);

      Thread thread1 = new Thread(() -> {
        try {
          startLatch.await();
          // race condition을 더 쉽게 만들기 위해 약간의 지연 추가
          Thread.sleep(1);
          instance1.set(Singleton.getInstance());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          endLatch.countDown();
        }
      });

      Thread thread2 = new Thread(() -> {
        try {
          startLatch.await();
          Thread.sleep(1);
          instance2.set(Singleton.getInstance());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          endLatch.countDown();
        }
      });

      thread1.start();
      thread2.start();
      startLatch.countDown();
      endLatch.await();

      if (instance1.get() != instance2.get()) {
        assertNotSame(instance1.get(), instance2.get());
        break;
      }
    }

  }
}