package com.ai.ollama.springai_ollama_llm.service.etl;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Log4j2
public class ETLProcessingService {
    @Autowired
    private EmbeddingModel embeddingModel;
    private final TextSplitter textSplitter;
    private final VectorStore vectorStore;

    public void ProcessETL(MultipartFile file) {

        log.info("Started ETL Process to the file {}", file.getOriginalFilename());

        try {

            TikaDocumentReader reader = new TikaDocumentReader(convertResource(file));
            List<Document> docs = reader.get();

            docs.forEach(d -> d.getMetadata().put("fileName", file.getOriginalFilename()));

            TokenTextSplitter splitter = TokenTextSplitter.builder()
                    .withChunkSize(1)
                    .withMinChunkSizeChars(1)
                    .withMinChunkLengthToEmbed(1)
                    .withMaxNumChunks(20)
                    .withKeepSeparator(true)
                    .build();

            List<Document> chunks = splitter.apply(docs);

            log.info("Total chunks created: {}", chunks.size());

            // 👉 ADD THIS: Inject EmbeddingModel in your class
            // @Autowired
            // private EmbeddingModel embeddingModel;

            for (int i = 0; i < chunks.size(); i++) {

                Document data = chunks.get(i);

                log.info("------------ CHUNK {} ------------", i);

                log.info("Metadata: {}", data.getMetadata());

                // Trim text to avoid huge logs
                String textPreview = data.getText().substring(0, Math.min(200, data.getText().length()));
                log.info("Text Preview: {}", textPreview);

                // ✅ APPROX TOKEN COUNT (simple approximation)
                int approxTokens = data.getText().length() / 4;
                log.info("Approx Token Count: {}", approxTokens);

                // ✅ REAL "PLOTTED" DATA → EMBEDDING
                EmbeddingResponse response = embeddingModel.embedForResponse(
                        List.of(data.getText())
                );

                float[] vector = response.getResults().get(0).getOutput();

                log.info("Embedding vector size: {}", vector.length);

                // Log only first few values (full vector is huge)
                log.info("Embedding sample: [{}, {}, {}, {} ...]",
                        vector[0], vector[1], vector[2], vector[3]);
            }

            // Store in vector DB
            vectorStore.add(chunks);

            log.info("ETL process completed successfully!");

        } catch (Exception e) {

            log.error("Exception occurred during ETL process", e); // ✅ proper logging
        }
    }

    private Resource convertResource(MultipartFile file) throws IOException {

        // Convert the uploaded MultipartFile into a Spring Resource
        // Resource is a generic abstraction used by Spring to handle file-like data
        // (can be file system, classpath, URL, or input stream)
        return new InputStreamResource(file.getInputStream());
    }


}
