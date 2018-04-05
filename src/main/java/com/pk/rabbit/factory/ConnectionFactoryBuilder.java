package com.pk.rabbit.factory;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

public class ConnectionFactoryBuilder {
  // 往spring动态注入ConnectionFactoryBuilder
  public static void connectionFactoryBuilder(ApplicationContext ctx, String host, int port,
      String username, String password) {
    // 获取BeanFactory
    DefaultListableBeanFactory defaultListableBeanFactory =
        (DefaultListableBeanFactory) ctx.getAutowireCapableBeanFactory();
    // 创建connectionFactory信息
    BeanDefinitionBuilder connectionFactoryBuilder =
        BeanDefinitionBuilder.genericBeanDefinition(CachingConnectionFactory.class);
    connectionFactoryBuilder.addPropertyValue("host", host);
    connectionFactoryBuilder.addPropertyValue("port", port);
    connectionFactoryBuilder.addPropertyValue("username", username);
    connectionFactoryBuilder.addPropertyValue("password", password);
    connectionFactoryBuilder.addPropertyValue("publisherConfirms", true);
    defaultListableBeanFactory.registerBeanDefinition("connectionFactory",
        connectionFactoryBuilder.getBeanDefinition());
  }
}
