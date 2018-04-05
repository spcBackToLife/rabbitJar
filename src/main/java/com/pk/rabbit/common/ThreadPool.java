package com.pk.rabbit.common;

import org.springframework.amqp.rabbit.connection.Connection;

import com.pk.rabbit.handle.MessageProcessInter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadPool {

  int threadCount;
  long interval;
  // String exchange;
  // String routingKey;
  String queue;
  Connection connection;
  MessageProcessInter messageProcess;

}
