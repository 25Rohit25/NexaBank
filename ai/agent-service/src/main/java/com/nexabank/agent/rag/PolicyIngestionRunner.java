package com.nexabank.agent.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "nexa.rag.ingest-on-startup", havingValue = "true")
public class PolicyIngestionRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(PolicyIngestionRunner.class);

    private final PolicyIngestionService ingestionService;

    public PolicyIngestionRunner(PolicyIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        int chunks = ingestionService.refresh();
        log.info("Refreshed Nexa Bank policy vector store with {} chunks", chunks);
    }
}
