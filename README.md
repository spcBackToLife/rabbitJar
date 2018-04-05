## 一丶介绍

​     rabbitMQJar是对rabbitmq的一种jar包封装，使其能简单的被java程序调用。在spring的rabbitTemplate基础上进一步简化使用。此项目参考了https://github.com/littlersmall/rabbitmq-access项目的多线程消费者与消息重发机制。也非常感谢作者的开源支持。

## 二丶封装功能支持

1. 支持发送字符串类型消息与java对象消息。
2. 支持多线程消费者。（固定线程池）
3. 支持消息重发，保证消息可靠到达。

## 三丶缺点

1. 消息重发机制缺陷：

     因发送消息至MQ后，需要MQ返回ACK确认发送成功，但在发送ACK返回前突然断网，则不知道是否发送成功。一定时间未收到ACK，则认定发送失败重发，而或许已经发送成功，只是还未有ACK确认。因此可能会造成消息多发。经过rabbitmq-access作者测试发送300w条数据，发送结束后，实际发送数据301.2w条。详细性能见：https://www.jianshu.com/p/4112d78a8753

## 四丶rabbitMQJar API

1. Rabbit:

     Rabbit对象里封装了设置连接，创建Exchange丶queue，接收消息功能。

2. SenderInter:

      发送消息接口。

3. SenderBuilder:

      发送消息对象创建者。可以创建一个发送消息对象。

4. MessageProcessInter:

     消息处理接口，用于接收到消息后给用户自己处理。

5. 设置连接属性接口:

   ```java
   public static void setConnection(String host,int port,String username,String password);                         --属于Rabbit类方法
   ```

6. 启动连接接口:

   ```java
   Rabbit rabbit = Rabbit.start();
   ```

7. 创建Exchange:

   ```java
   void createExchange(String exchange,String routingKey,String type,boolean durable,boolean autoDelete,Connection connection)
   type: direct丶fanout丶 topic
   durable:　是否持久化exchange，即重启是否保留这个exchange。
   autoDelete: 没有消费者的时候，服务器是否删除exchange.
   Connection: rabbitTemplate的连接对象。
    											--属于Rabbit对象方法
   ```

8. 创建队列并绑定exchange:

   ```java
   void createQueueAndBindExchange(String queue,boolean durable,boolean exclusive,boolean autoDelete,Connection connection);
   durable:　在服务器重启时，队列是否不删除.
   exclusive: 是否为当前连接的专用队列。(专用则在连接断开后，会自动删除该队列）
   autoDelete: 当没有任何消费者使用时，自动删除该队列。
   Connection: rabbitTemplate的连接对象。
    											--属于Rabbit对象方法
   ```

9. 创建发送者:

   ```java
   SenderInter buildMessageSender(String exchange,String routingKey,String queue,Rabbit rabbit);
   其中queue参数可为空串，表示只往exchange发消息，不直接发送到队列，而是y与exchange绑定的队列可以接收到消息。
   										--属于SenderBuilder类方法
   ```

10. 发送消息:

    ```java
    void send(String message);// 发送字符串
    void send(Object message);// 发送java对象
                                              --属于SenderInter接口方法
    ```

11. 接收消息:

    ```java
    void threadPoolConsume(int threadCount,int interval,String queue,MessageProcessInter messageProcess);
    threadCount: 消费者线程数量。
    interval: 每个线程消费消息的间隔时间。
    messageProcess: 消息处理对象。（用户自定义对象继承MessageProcessInter接口）。
    ```

## 五丶例子

1. 导入jar包：rabbitMQ.jar。

2. 设置并启动连接:

   ```java
      Rabbit.setConnection("127.0.0.1", 5672, "username", "password");// 设置连接的ip丶端口丶用户名丶密码
      Rabbit rabbit = Rabbit.start();// 启动连接
   ```

   ​


3. 创建Exchange:

   ```java
   rabbit.createExchange("exchange", "routingKey","type",true,false,rabbit.getConnection());
   ```

4. 创建Queue:

   ```java
   rabbit.createQueueAndBindExchange("queue",true,false,false,rabbit.getConnection());
   ```

   ​


5. 创建消息发送者:

   ```java
   SenderInter sender=SenderBuilder.buildMessageSender("exchange","",rabbit);
   //发送string消息
   sender.send("我是rabbit");
   //发送对象消息
   sender.send(new ConsumeFlag(true,"i am rabbit")); //对象用户自定义，此处只是例子。
   ```

   ​


6. 创建消息处理类:

   ```java
   package com.pk.rabbit;
   import java.io.UnsupportedEncodingException;
   import org.springframework.amqp.core.Message;
   import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
   import org.springframework.amqp.support.converter.MessageConverter;
   import com.pk.rabbit.common.SuccessFlag;
   import com.pk.rabbit.handle.MessageProcessInter;

   public class MyProcess implements MessageProcessInter {

     @Override
     public SuccessFlag process(Message message) {
       MessageConverter messageConverter = new Jackson2JsonMessageConverter();
       // 对象接收
       // ConsumeFlag cf = (ConsumeFlag) messageConverter.fromMessage(message);
       // System.out.println(cf.getError());
       // 字符串接收
       try {
         System.out.println(new String(message.getBody(), "UTF-8"));
       } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
       }
       return new SuccessFlag(true, "");
     }
   }
   ```

7. 创建消息处理对象:

   ```java
   MyProcess mp=new MyProcess();
   ```

8. 开启线程池消费者:

   ```java
   rabbit.threadPoolConsume(2,1,"queue",mp);
   ```



github: https://github.com/spcBackToLife/rabbitJar
