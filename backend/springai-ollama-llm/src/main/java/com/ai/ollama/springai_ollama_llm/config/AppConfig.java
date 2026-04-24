package com.ai.ollama.springai_ollama_llm.config;

import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    // Marks this method as a Spring Bean
    // Spring will manage its lifecycle and make it available for dependency injection
    @Bean
    TextSplitter textSplitter() {

        // Create and return a TokenTextSplitter instance
        // This splitter is used to break large text into smaller chunks
        // Useful for LLM processing, embeddings, and vector storage
        return new TokenTextSplitter();
    }
}