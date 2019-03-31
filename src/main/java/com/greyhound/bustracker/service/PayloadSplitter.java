package com.greyhound.bustracker.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.integration.annotation.Splitter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class PayloadSplitter {

  @Splitter
  public Collection<String> splitAndPublish(Message<String> message) {
    final Collection<String> messages = new ArrayList<>();

    final JSONArray records = new JSONObject(message.getPayload()).getJSONArray("results");

    for (int i = 0; i < records.length(); i++) {
      messages.add(String.valueOf(records.get(i)));
    }

    return messages;
  }
}

