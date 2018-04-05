package com.pk.rabbit.domain;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

import com.pk.rabbit.Rabbit;
import com.pk.rabbit.cache.MessageCache;
import com.pk.rabbit.common.SuccessFlag;
import com.pk.rabbit.factory.RabbitTemplateBuilder;

public class Sender implements SenderInter {

  private static RabbitTemplate rabbitTemplate;

  private MessageCache messageCache;

  public Sender(String exchange, String routingKey, String queue, Rabbit rabbit) {
    messageCache = new MessageCache();
    messageCache.setSender(this);
    rabbitTemplate = RabbitTemplateBuilder.createRabbitTemplate(exchange, routingKey, queue,
        rabbit.getConnectionFactory(), messageCache);
  }

  @Override
  public SuccessFlag send(Object message) {
    try {
      String id = messageCache.generateId();
      messageCache.add(id, message);
      rabbitTemplate.correlationConvertAndSend(message, new CorrelationData(id));
      return new SuccessFlag(true, "");
    } catch (Exception e) {
      e.printStackTrace();
      return new SuccessFlag(false, e.getMessage());
    }
  }


}
