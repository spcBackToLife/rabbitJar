package com.pk.rabbit.factory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.amqp.rabbit.connection.Connection;

import com.pk.rabbit.common.Constant;
import com.pk.rabbit.common.SuccessFlag;
import com.pk.rabbit.common.ThreadPool;
import com.pk.rabbit.domain.Consumer;
import com.pk.rabbit.domain.ConsumerInter;
import com.pk.rabbit.handle.MessageProcessInter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadPoolConsumerBuilder {
  private ExecutorService executor;
  private volatile boolean stop = false;
  private final ThreadPool threadPool;
  private static volatile ThreadPoolConsumerBuilder threadPoolConsumerBuilder;

  private ThreadPoolConsumerBuilder(int threadCount, long interval, String queue,
      Connection connection, MessageProcessInter messageProcess) {
    // 创建固定的线程池,这里可以改变一下
    this.threadPool = new ThreadPool(threadCount, interval, queue, connection, messageProcess);
    executor = Executors.newFixedThreadPool(threadPool.getThreadCount());
  }

  // 得到线程池创建实例对象.
  public static ThreadPoolConsumerBuilder getInstance(int threadCount, long interval, String queue,
      Connection connection, MessageProcessInter messageProcess) {
    if (threadPoolConsumerBuilder == null) {
      synchronized (ThreadPoolConsumerBuilder.class) {
        if (threadPoolConsumerBuilder == null) {
          threadPoolConsumerBuilder = new ThreadPoolConsumerBuilder(threadCount, interval, queue,
              connection, messageProcess);
        }
      }
    }
    return threadPoolConsumerBuilder;
  }

  private ConsumerInter consumerBuilder(Connection connection, String queue,
      MessageProcessInter messageProcess) {
    return new Consumer(connection, queue, messageProcess);
  }

  public void threadPoolConsume() {
    for (int i = 0; i < threadPool.getThreadCount(); i++) {
      final ConsumerInter messageConsumer = this.consumerBuilder(this.threadPool.getConnection(),
          this.threadPool.getQueue(), this.threadPool.getMessageProcess());
      executor.execute(new Runnable() {
        @Override
        public void run() {
          while (!stop) {
            try {
              SuccessFlag successFlag = messageConsumer.consume();

              if (threadPool.getInterval() > 0) {
                try {
                  Thread.sleep(threadPool.getInterval());
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
              if (!successFlag.isSuccess()) {
                log.info("run error " + successFlag.getError());
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      });
      // JVM关闭前进行内存清理
      Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }
  }

  public void stop() {
    this.stop = true;
    try {
      Thread.sleep(Constant.ONE_SECOND);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
