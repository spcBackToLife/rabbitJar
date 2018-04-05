package com.pk.rabbit.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.pk.rabbit.common.Constant;
import com.pk.rabbit.common.SuccessFlag;
import com.pk.rabbit.domain.Sender;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageCache {
  private Sender sender;
  private boolean stop = false;
  private Map<String, MessageWithTime> map = new ConcurrentHashMap<>();
  private AtomicLong id = new AtomicLong();

  public MessageCache() {}

  private class MessageWithTime {
    long time;
    Object message;

    public long getTime() {
      return time;
    }

    public MessageWithTime(long time, Object message) {
      this.time = time;
      this.message = message;
    }

    public Object getMessage() {
      return message;
    }


  }

  public void setSender(Sender sender) {
    this.sender = sender;
    startRetry();
  }

  public String generateId() {
    return "" + id.incrementAndGet();
  }

  public void add(String id, Object message) {
    map.put(id, new MessageWithTime(System.currentTimeMillis(), message));
  }

  public void del(String id) {
    map.remove(id);
  }

  private void startRetry() {
    new Thread(() -> {
      while (!stop) {
        try {
          Thread.sleep(Constant.RETRY_TIME_INTERVAL);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        long now = System.currentTimeMillis();

        for (String key : map.keySet()) {
          MessageWithTime messageWithTime = map.get(key);

          if (null != messageWithTime) {
            if (messageWithTime.getTime() + 3 * Constant.VALID_TIME < now) {
              log.info("send message failed after 3 min " + messageWithTime);
              del(key);
            } else if (messageWithTime.getTime() + Constant.VALID_TIME < now) {
              SuccessFlag successFlag = sender.send(messageWithTime.getMessage());

              if (successFlag.isSuccess()) {
                del(key);
              }
            }
          }
        }
      }
    }).start();
  }
}
