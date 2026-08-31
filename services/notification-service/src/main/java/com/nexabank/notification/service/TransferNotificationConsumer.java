package com.nexabank.notification.service;

import com.nexabank.notification.event.TransferCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class TransferNotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(TransferNotificationConsumer.class);
    private final ObjectMapper objectMapper;

    public TransferNotificationConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "bank.transfer.completed")
    public void notifyTransferParties(String json) throws Exception {
        TransferCompletedEvent event = objectMapper.readValue(json, TransferCompletedEvent.class);
        log.info("notification=transfer_debit customerId={} accountId={} transferId={} amount={} currency={} correlationId={}",
                event.sourceCustomerId(), event.sourceAccountId(), event.transferId(), event.amount(),
                event.currency(), event.correlationId());
        log.info("notification=transfer_credit customerId={} accountId={} transferId={} amount={} currency={} correlationId={}",
                event.destinationCustomerId(), event.destinationAccountId(), event.transferId(), event.amount(),
                event.currency(), event.correlationId());
    }
}
