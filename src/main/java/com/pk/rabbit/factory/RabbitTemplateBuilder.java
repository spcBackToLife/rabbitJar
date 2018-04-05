package com.pk.rabbit.factory;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.pk.rabbit.cache.MessageCache;
import com.pk.rabbit.common.Constant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitTemplateBuilder {
  public static RabbitTemplate createRabbitTemplate(final String exchange, final String routingKey,
      final String queue, ConnectionFactory connectionFactory, MessageCache messageCache) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMandatory(true);
    rabbitTemplate.setExchange(exchange);
    rabbitTemplate.setRoutingKey(routingKey);
    // 设置消息序列化方法,传对象消息需要使用.
    // rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

    // 设置回调,消息发送到rabbitMQ的时候,会调用此回调.
    rabbitTemplate.setConfirmCallback((correlationData, ack, err) -> {
      if (!ack) {
        log.info("发送失败: " + err + correlationData.toString());
      } else {
        messageCache.del(correlationData.getId());
      }
    });
    // 如果没有exchange和队列接收此消息,则会回调
    rabbitTemplate
        .setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
          try {
            Thread.sleep(Constant.ONE_SECOND);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          // 发送失败
          log.info("发送失败: " + replyCode + ":" + replyText);
          // Send a message to a default exchange with a specific routing key.
          // 此处并没什么用,只是说在没有队列和exchange的情况下,往一个默认的exchange发送.
          rabbitTemplate.send(message);
        });
    return rabbitTemplate;
  }
}
