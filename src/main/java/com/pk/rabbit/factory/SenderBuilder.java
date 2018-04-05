package com.pk.rabbit.factory;

import com.pk.rabbit.Rabbit;
import com.pk.rabbit.domain.Sender;
import com.pk.rabbit.domain.SenderInter;

public class SenderBuilder {
  public static SenderInter buildMessageSender(final String exchange, final String routingKey,
      final String queue, final Rabbit rabbit) {
    // 1. 构造rabbitMQTemplate模块
    return new Sender(exchange, routingKey, queue, rabbit);
  }
}
