package com.pk.rabbit.domain;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;

import com.pk.rabbit.common.Constant;
import com.pk.rabbit.common.SuccessFlag;
import com.pk.rabbit.handle.MessageProcessInter;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Consumer implements ConsumerInter {
  QueueingConsumer consumer;
  MessageProcessInter messageProcess;
  Connection connection;
  String queue;
  private static MessagePropertiesConverter messagePropertiesConverter;

  public Consumer(Connection connection, String queue, MessageProcessInter messageProcess) {
    this.connection = connection;
    this.queue = queue;
    this.messageProcess = messageProcess;
    messagePropertiesConverter = new DefaultMessagePropertiesConverter();
    consumer = buildQueueConsumer(connection, queue);
  }

  @Override
  public SuccessFlag consume() {
    QueueingConsumer.Delivery delivery;
    Channel channel = consumer.getChannel();
    try {

      delivery = consumer.nextDelivery();
      Message message = new Message(delivery.getBody(), messagePropertiesConverter
          .toMessageProperties(delivery.getProperties(), delivery.getEnvelope(), "UTF-8"));
      SuccessFlag successFlag;
      try {
        // 在用户处理消息前，可以进行处理？,处理完之后return
        successFlag = messageProcess.process(message);
        // 在用户处理消息后，可进行处理？
      } catch (Exception e) {
        successFlag = new SuccessFlag(false, "process exception: " + e);
        e.printStackTrace();
      }
      // 确认消息发送成功,就返回ACK
      if (successFlag.isSuccess()) {
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      } else {
        Thread.sleep(Constant.ONE_SECOND);
        log.info("process message failed: " + successFlag.getError());
        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
      }
      return successFlag;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return new SuccessFlag(false, "interrupted exception " + e.toString());
    } catch (ShutdownSignalException | ConsumerCancelledException | IOException e) {
      e.printStackTrace();
      closeConnection(channel);
      // 如果走到这,则表示最初的连接未成功,或者是中途断开了,因此重新连接.
      consumer = buildQueueConsumer(connection, queue);
      return new SuccessFlag(false, "shutdown or cancelled exception " + e.toString());
    } catch (Exception e) {
      e.printStackTrace();
      log.info("exception : ", e);
      closeConnection(channel);
      // 如果走到这,则表示最初的连接未成功,或者是中途断开了,因此重新连接.
      consumer = buildQueueConsumer(connection, queue);
      return new SuccessFlag(false, "exception " + e.toString());
    }
  }

  private void closeConnection(Channel channel) {
    try {
      channel.close();
      log.info("close channel");;
    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
      log.info("close channel time out ", e);
    }
  }

  private QueueingConsumer buildQueueConsumer(Connection connection, String queue) {
    try {
      Channel channel = connection.createChannel(false);
      QueueingConsumer consumer = new QueueingConsumer(channel);
      // 通过 BasicQos 方法设置prefetchCount = 1。这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。
      // 换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它
      channel.basicQos(1);
      channel.basicConsume(queue, false, consumer);
      return consumer;
    } catch (Exception e) {
      e.printStackTrace();
      log.info("build queue consumer error : ", e);
      try {
        Thread.sleep(Constant.ONE_SECOND);
      } catch (InterruptedException inE) {
        inE.printStackTrace();
      }
      // 构建消费者失败的话进行处理.
      return buildQueueConsumer(connection, queue);
    }
  }

}
