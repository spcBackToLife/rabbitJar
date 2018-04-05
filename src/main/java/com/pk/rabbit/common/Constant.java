package com.pk.rabbit.common;

public class Constant {
  // 线程数
  public final static int THREAD_COUNT = 5;

  // 处理间隔时间
  public final static int INTERVAL_MILS = 0;

  // consumer失败后等待时间(mils)
  public static final int ONE_SECOND = 1 * 1000;

  // 异常sleep时间(mils)
  public static final int ONE_MINUTE = 1 * 60 * 1000;
  // MQ消息retry时间
  public static final int RETRY_TIME_INTERVAL = 1 * 60 * 1000;
  // MQ消息有效时间
  public static final int VALID_TIME = 1 * 60 * 1000;
  // 创建默认的队列类型
  public static final String DEFAULT_EXCHANGE_TYPE = "fanout";
  public static final String FANOUT = "fanout";
  public static final String TOPIC = "topic";
}
