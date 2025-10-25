package org.example.ugv_rc.clients;

import static org.apache.hc.client5.http.impl.classic.HttpClients.createDefault;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.StatusLine;

@Slf4j
public class JetsonOrinNanoClient {

  private final String host;

  public JetsonOrinNanoClient(String host) {
    this.host = host;
  }

  public JsonNode get(String path) throws RuntimeException {
    JsonFactory jsonFactory = new JsonFactory();
    ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
    JsonNode responseData;
    try (CloseableHttpClient client = createDefault()) {
      ClassicHttpRequest httpGet = ClassicRequestBuilder.get()
          .setScheme("http")
          .setHttpHost(new HttpHost(host, 8000))
          .setPath(path)
          .build();
      log.info("Request: {}", path);
      responseData = client.execute(httpGet, response -> {
        if (response.getCode() >= 300) {
          log.error(new StatusLine(response).toString());
          client.close();
          throw new RuntimeException("JetsonClientError");
        }
        final HttpEntity responseEntity = response.getEntity();
        if (responseEntity == null) {
          return null;
        }
        try (InputStream inputStream = responseEntity.getContent()) {
          return objectMapper.readTree(inputStream);
        }
      });
      if (responseData != null) {
        if (!responseData.isEmpty()) {
          log.info("Response: {}", responseData);
        }
      }
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new RuntimeException("JetsonClientError");
    }
    return responseData;
  }
}
