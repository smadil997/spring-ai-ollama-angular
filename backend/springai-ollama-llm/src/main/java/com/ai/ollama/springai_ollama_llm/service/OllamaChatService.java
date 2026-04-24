package com.ai.ollama.springai_ollama_llm.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Service
@Log4j2
public class OllamaChatService {

    // Chat model used to generate responses (Ollama LLM)
    private final OllamaChatModel chatModel;

    // Vector store used for semantic search (RAG)
    private final VectorStore vectorStore;

    // Embedding model used to convert text into vectors
    @Autowired
    private OllamaEmbeddingModel embeddingModel;

    // ❌ Constructor (currently problematic – explained below)
    public OllamaChatService(OllamaChatModel chatModel,
                             VectorStore vectorStore,
                             OllamaChatModel chatModel1,
                             OllamaEmbeddingModel ollamaEmbeddingModel,
                             VectorStore vectorStore1) {

        // Assigning duplicate parameters (confusing + incorrect usage)
        this.chatModel = chatModel1;
        this.vectorStore = vectorStore1;
    }


    // Method to stream response token-by-token (reactive streaming)
    public Flux<String> processMessage(String messagePrompt) {
        // Create prompt object from user input
        Prompt prompt = new Prompt(messagePrompt);
        return chatModel.stream(prompt)   // Stream response from LLM

                // Extract only text from ChatResponse
                .map(chatResponse -> {
                    return chatResponse.getResult().getOutput().getText();
                })
                // Add artificial delay between tokens (for UI streaming effect)
                .delayElements(Duration.ofSeconds(1));
    }


    // RAG-based response (uses Vector DB + LLM)
    public String getEmbeddedModelResp(String userText) {

        return ChatClient.builder(chatModel)   // Build chat client

                .build()
                .prompt()

                // Connect vector store for retrieving relevant chunks
                .advisors(new QuestionAnswerAdvisor(vectorStore))

                .user(userText)                // User query
                .call()                        // Execute request
                .chatResponse()                // Get response object
                .getResult()
                .getOutput()
                .getText();                   // Final answer
    }


    // Stream full ChatResponse (not just text)
    public Flux<ChatResponse> processMessageChatResponse(String gemma34Dtos) {
        // Returns full response including metadata (tokens, role, etc.)
        return chatModel.stream(new Prompt(gemma34Dtos));
    }
}