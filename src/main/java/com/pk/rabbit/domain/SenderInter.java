package com.pk.rabbit.domain;

import com.pk.rabbit.common.SuccessFlag;

public interface SenderInter {
  // 对象发送
  SuccessFlag send(Object message);
}
