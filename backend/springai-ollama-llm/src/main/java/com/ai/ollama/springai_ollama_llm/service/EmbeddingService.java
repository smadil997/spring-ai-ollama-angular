package com.ai.ollama.springai_ollama_llm.service;

import com.ai.ollama.springai_ollama_llm.service.etl.ETLProcessingService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Log4j2
@Service
@AllArgsConstructor
public class EmbeddingService {


    // Method to process multiple uploaded PDF files
    public void processEmbeddingPDFFile(List<MultipartFile> files) {
        // Loop through each file and call ETL process
        // Method reference (::ProcessETL) is shorthand for file -> etlProcessingService.ProcessETL(file)
        files.forEach(etlProcessingService::ProcessETL);
    }


//    List<Document> similarSearch= vectorStore.similaritySearch(userText);
//        log.info("------------  ------------", similarSearch);
//
//        for (int i = 0; i < similarSearch.size(); i++) {
//
//        Document data = similarSearch.get(i);
//
//        log.info("------------ similarSearch {} ------------", i);
//
//        log.info("Metadata: {}", data.getMetadata());
//
//        // Trim text to avoid huge logs
//        String textPreview = data.getText().substring(0, Math.min(200, data.getText().length()));
//        log.info("Text Preview: {}", textPreview);
//
//        // ✅ APPROX TOKEN COUNT (simple approximation)
//        int approxTokens = data.getText().length() / 4;
//        log.info("Approx Token Count: {}", approxTokens);
//    }


    // VectorStore: used to store and retrieve embeddings (chunks of documents)
    private final VectorStore vectorStore;
    // Service responsible for ETL (Extract → Transform → Load)
    private final ETLProcessingService etlProcessingService;
    // Chat model (Ollama LLM) used to generate responses
    private final OllamaChatModel chatModel;

    private final EmbeddingModel embeddingModel; // 👈 Inject this to inspect raw embeddings

    public String getChatWithEmbeddedServer(String userText) {

        // ─────────────────────────────────────────────
        // STEP 1: Show the raw user query
        // ─────────────────────────────────────────────
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📝 STEP 1 — USER QUERY RECEIVED");
        log.info("   Query: \"{}\"", userText);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // ─────────────────────────────────────────────
        // STEP 2: Convert user query → embedding vector
        // The embedding model tokenizes your text and
        // maps it to a high-dimensional float[] vector.
        // Each number captures semantic meaning.
        // ─────────────────────────────────────────────
        log.info("🔢 STEP 2 — CONVERTING QUERY TO EMBEDDING VECTOR...");
        float[] queryEmbedding = embeddingModel.embed(userText);  // calls e.g. nomic-embed-text

        log.info("   Model used     : {}", embeddingModel.getClass().getSimpleName());
        log.info("   Vector dims    : {} (each = one semantic dimension)", queryEmbedding.length);
        log.info("   First 10 values: {}", Arrays.toString(Arrays.copyOf(queryEmbedding, 10)));
        log.info("   → Your text is now a point in {}-dimensional space", queryEmbedding.length);

        // ─────────────────────────────────────────────
        // STEP 3: Search vector store using cosine similarity
        // The query vector is compared against all stored
        // document chunk vectors. Closest = most relevant.
        // ─────────────────────────────────────────────
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🔍 STEP 3 — SEMANTIC VECTOR SEARCH IN VECTOR STORE");

        // Manually run the same similarity search QuestionAnswerAdvisor will do internally
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userText)
                        .topK(4)                // Top 4 closest chunks
                        .similarityThreshold(0.5) // Min cosine similarity score
                        .build()
        );

        log.info("   Top {} similar chunks found:", similarDocs.size());
        for (int i = 0; i < similarDocs.size(); i++) {
            Document doc = similarDocs.get(i);
            String preview = doc.getText().length() > 120
                    ? doc.getText().substring(0, 120) + "..."
                    : doc.getText();

            log.info("   ┌── Chunk #{}", i + 1);
            log.info("   │  Score    : {}", doc.getMetadata().getOrDefault("distance", "N/A"));
            log.info("   │  Source   : {}", doc.getMetadata().getOrDefault("source", "unknown"));
            log.info("   │  Content  : \"{}\"", preview);
            log.info("   └──────────────────────────────────────────");
        }

        // ─────────────────────────────────────────────
        // STEP 4: Build the RAG prompt (augmented prompt)
        // QuestionAnswerAdvisor stuffs the retrieved chunks
        // into the prompt as context before your question.
        //
        // Final prompt sent to LLM looks like:
        //   "Context: <chunk1> <chunk2>... Question: <userText>"
        // ─────────────────────────────────────────────
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📦 STEP 4 — BUILDING AUGMENTED PROMPT FOR LLM");
        log.info("   QuestionAnswerAdvisor will inject the {} chunks above", similarDocs.size());
        log.info("   as context into the prompt before calling the LLM.");
        log.info("   Prompt structure:");
        log.info("   ┌──────────────────────────────────────────────");
        log.info("   │  [SYSTEM]  You are a helpful assistant.");
        log.info("   │  [CONTEXT] <chunk1 text>");
        log.info("   │            <chunk2 text> ...");
        log.info("   │  [USER]    \"{}\"", userText);
        log.info("   └──────────────────────────────────────────────");

        // ─────────────────────────────────────────────
        // STEP 5: Call LLM with the augmented prompt
        // ─────────────────────────────────────────────
        log.info("🤖 STEP 5 — CALLING LLM (OllamaChatModel)...");
        long start = System.currentTimeMillis();

        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);

        ChatResponse chatResponse = ChatClient.builder(chatModel)
                .build()
                .prompt()
                .advisors(questionAnswerAdvisor)
                .user(userText)
                .call()
                .chatResponse();

        long elapsed = System.currentTimeMillis() - start;

        // ─────────────────────────────────────────────
        // STEP 6: Inspect the response metadata
        // ─────────────────────────────────────────────
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("✅ STEP 6 — LLM RESPONSE RECEIVED");
        log.info("   Time taken     : {} ms", elapsed);
        log.info("   Finish reason  : {}", chatResponse.getResult().getMetadata().getFinishReason());

        // Token usage (if Ollama reports it)
        var usage = chatResponse.getMetadata().getUsage();
        if (usage != null) {
            log.info("   Prompt tokens  : {}", usage.getPromptTokens());
            log.info("   Response tokens: {}", usage.getCompletionTokens());
            log.info("   Total tokens   : {}", usage.getTotalTokens());
        }

        AssistantMessage output = chatResponse.getResult().getOutput();
        String finalText = output.getText();

        log.info("   Response preview: \"{}\"",
                finalText.length() > 200 ? finalText.substring(0, 200) + "..." : finalText);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return finalText;
    }


    // Method to ask questions using embedded data (RAG - Retrieval Augmented Generation)
    public String getChatWithEmbeddedServer1(String userText) {
        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);
        return ChatClient.builder(chatModel)   // Create ChatClient using Ollama model
                .build()                      // Build the client instance
                .prompt()                     // Start building the prompt
                // Add advisor that connects VectorStore for retrieving relevant document chunks
                // This enables semantic search (RAG)
                .advisors(questionAnswerAdvisor)
                .user(userText)               // Set user input/question
                .call()                       // Send request to LLM
                .chatResponse()               // Get full chat response object
                .getResult()                  // Extract result wrapper
                .getOutput()                  // Get model output
                .getText();                  // Final response text
    }


}
