package com.nexabank.agent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class PolicyDocumentLoader {
    private static final String POLICY_PATTERN = "classpath*:/rag-documents/*.md";

    public List<Document> load() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(POLICY_PATTERN);
            Arrays.sort(resources, Comparator.comparing(Resource::getFilename));
            List<Document> policies = new ArrayList<>();
            for (Resource resource : resources) {
                String text = resource.getContentAsString(StandardCharsets.UTF_8);
                TextReader reader = new TextReader(resource);
                reader.getCustomMetadata().put("documentType", "bank_policy");
                reader.getCustomMetadata().put("policyId", requiredHeader(text, "Policy ID:"));
                reader.getCustomMetadata().put("effectiveDate", requiredHeader(text, "Effective date:"));
                policies.addAll(reader.read());
            }
            if (policies.isEmpty()) {
                throw new IllegalStateException("No policy documents found at " + POLICY_PATTERN);
            }
            return TokenTextSplitter.builder()
                    .withChunkSize(300)
                    .withMinChunkSizeChars(100)
                    .withMinChunkLengthToEmbed(20)
                    .withKeepSeparator(true)
                    .build()
                    .apply(policies);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load Nexa Bank policy documents", exception);
        }
    }

    private String requiredHeader(String text, String prefix) {
        return text.lines()
                .map(String::trim)
                .filter(line -> line.startsWith(prefix))
                .map(line -> line.substring(prefix.length()).trim())
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Policy document is missing " + prefix));
    }
}
