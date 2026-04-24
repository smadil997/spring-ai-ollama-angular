package com.ai.ollama.springai_ollama_llm.controller;


import com.ai.ollama.springai_ollama_llm.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/embedding")
public class EmbeddingController {


    @Autowired
    private EmbeddingService embeddingService;


    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdfFIleForEmbedding(@RequestParam("files") List<MultipartFile> files){
        embeddingService.processEmbeddingPDFFile(files);
        return new ResponseEntity<>("Uploaded and processing started", HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "/chat/{prompt}")
    public String getChatWithEmbeddedServer(@PathVariable String prompt) {
        return embeddingService.getChatWithEmbeddedServer(prompt);
    }
}
