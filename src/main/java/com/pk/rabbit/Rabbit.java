package com.pk.rabbit;

import javax.annotation.PostConstruct;

import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.pk.rabbit.factory.ConnectionFactoryBuilder;
import com.pk.rabbit.factory.ExchangeAndQueueBuilder;
import com.pk.rabbit.factory.ThreadPoolConsumerBuilder;
import com.pk.rabbit.handle.MessageProcessInter;

@Service
public class Rabbit {
  @Autowired
  private ConnectionFactory connectionFactory;

  private static Connection connection;
  // 此处是否要单例
  private static ApplicationContext ctx =
      new ClassPathXmlApplicationContext("applicationContext.xml");

  private Rabbit() {
    System.out.println("实例初始化时调用:");
  }

  public ConnectionFactory getConnectionFactory() {
    return connectionFactory;
  }

  public Connection getConnection() {
    return connection;
  }

  @PostConstruct
  public void init() {
    System.out.println("[*]创建连接...");
    // TODO: PK 做连接中断判断,或者在使用的地方判断链接是否断开.
    connection = connectionFactory.createConnection();
  }

  public static void setConnection(String host, int port, String username, String password) {
    System.out.println("[*]设置初始连接...");
    ConnectionFactoryBuilder.connectionFactoryBuilder(ctx, host, port, username, password);
  }

  public void createExchange(String exchange, String routingKey, String type, boolean durable,
      boolean autoDelete, Connection connection) {
    ExchangeAndQueueBuilder.createExchange(exchange, routingKey, type, durable, autoDelete,
        connection);
  }

  public void createQueueAndBindExchange(String queue, boolean durable, boolean exclusive,
      boolean autoDelete, Connection connection, String exchange, String routingKey) {
    ExchangeAndQueueBuilder.createQueueAndBindExchange(queue, durable, exclusive, autoDelete,
        connection, exchange, routingKey);
  }

  public <T> void threadPoolConsume(int threadCount, int interval, String queue,
      MessageProcessInter messageProcess) {
    ThreadPoolConsumerBuilder.getInstance(threadCount, interval, queue, connection, messageProcess)
        .threadPoolConsume();
  }

  // 创建rabbitMQ连接
  public static Rabbit start() {
    System.out.println("Rabbit.start");
    return ctx.getBean(Rabbit.class);
  }
}
