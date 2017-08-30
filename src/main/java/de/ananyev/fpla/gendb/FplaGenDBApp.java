package de.ananyev.fpla.gendb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Created by Ilya Ananyev on 24.12.16.
 */
@EnableDiscoveryClient
@SpringBootApplication
public class FplaGenDBApp {
  public static void main(String... agrg) {
    SpringApplication.run(FplaGenDBApp.class, agrg);
  }
}
