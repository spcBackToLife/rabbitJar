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
    // ConsumeFlag cf = (ConsumeFlag) messageConverter.fromMessage(message);
    // System.out.println(cf.getError());
    try {
      System.out.println(new String(message.getBody(), "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return new SuccessFlag(true, "");
  }

}
