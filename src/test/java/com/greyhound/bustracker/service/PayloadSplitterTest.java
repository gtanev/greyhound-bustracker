package com.greyhound.bustracker.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class PayloadSplitterTest {

  private PayloadSplitter payloadSplitter;
  private Message<String> message;

  @BeforeEach
  void init() throws IOException {
    Resource resource = new ClassPathResource("test-data.json");
    String jsonPayload = new String(Files.readAllBytes(resource.getFile().toPath()));

    message = new Message<String>() {
      @Override
      public String getPayload() {
        return jsonPayload;
      }

      @Override
      public MessageHeaders getHeaders() {
        return null;
      }
    };

    payloadSplitter = new PayloadSplitter();
  }

  @Test
  void testSplitter() {
    Collection<String> splitPayload = payloadSplitter.splitAndPublish(message);

    JSONArray objects = new JSONObject(message.getPayload()).getJSONArray("results");

    assertNotNull(splitPayload);
    assertEquals(objects.length(), splitPayload.size());
  }

}
