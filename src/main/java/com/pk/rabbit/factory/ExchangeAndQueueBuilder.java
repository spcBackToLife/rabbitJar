package com.pk.rabbit.factory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.rabbit.connection.Connection;

import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExchangeAndQueueBuilder {
  public static void createExchange(String exchange, String routingKey, String type,
      boolean durable, boolean autoDelete, Connection connection) {
    Channel channel = connection.createChannel(false);
    try {
      channel.exchangeDeclare(exchange, type, durable, autoDelete, null);
    } catch (IOException e) {
      System.out.println("how to fix?");
      e.printStackTrace();
    }
    closeConnection(channel);
  }

  public static void createQueueAndBindExchange(String queue, boolean durable, boolean exclusive,
      boolean autoDelete, Connection connection, String exchange, String routingKey) {
    Channel channel = connection.createChannel(false);
    try {
      channel.queueDeclare(queue, true, false, false, null);
      channel.queueBind(queue, exchange, routingKey);
    } catch (IOException e) {
      // 如何处理?
      System.out.println("how to fix?");
      e.printStackTrace();
    }
    closeConnection(channel);
  }

  private static void closeConnection(Channel channel) {
    try {
      try {
        channel.close();
      } catch (IOException e) {
        // 如何处理?
        System.out.println("how to fix?");
        e.printStackTrace();
      }
      log.info("close channel");;
    } catch (TimeoutException e) {
      e.printStackTrace();
      log.info("close channel time out ", e);
    }
  }
}
