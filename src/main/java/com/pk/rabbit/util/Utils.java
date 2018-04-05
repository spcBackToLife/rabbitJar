package com.pk.rabbit.util;

import org.apache.commons.codec.binary.Base64;

import com.alibaba.fastjson.JSONObject;

public class Utils {

  public static String toString(Object message) {
    String msg = JSONObject.toJSONString(message);
    msg = msg.substring(1, msg.length() - 1);
    return new String(Base64.decodeBase64(msg));
  }

  public static JSONObject toJson(Object message) {
    String msg = JSONObject.toJSONString(message);
    msg = msg.substring(1, msg.length() - 1);
    msg = new String(Base64.decodeBase64(msg));
    return JSONObject.parseObject(msg);
  }
}
