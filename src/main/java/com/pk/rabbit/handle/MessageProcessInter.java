package com.pk.rabbit.handle;

import org.springframework.amqp.core.Message;

import com.pk.rabbit.common.SuccessFlag;

public interface MessageProcessInter {
  SuccessFlag process(Message message);
}
