package com.nexabank.account.integration;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class KafkaIntegrationTest {

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:3.8.0"));

    @Test
    void brokerAcceptsAdministrativeRequests() throws Exception {
        Map<String, Object> config = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5_000);

        try (AdminClient admin = AdminClient.create(config)) {
            assertThat(admin.describeCluster().clusterId().get(5, TimeUnit.SECONDS)).isNotBlank();
        }
    }
}
