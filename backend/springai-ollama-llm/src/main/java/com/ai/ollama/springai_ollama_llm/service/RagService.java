package com.ai.ollama.springai_ollama_llm.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Log4j2
public class RagService {

    private final VectorStore vectorStore;

    @Value("classpath:/*.pdf")
    private Resource[] pdfFiles;


    public RagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingestPDFDataIntoDB(String fileName) throws IOException {
//        TextSplitter textSplitter = new TokenTextSplitter();
//        if (fileName.equalsIgnoreCase("ALL")) {
//            for (Resource pdfFile : pdfFiles) {
//                log.info("File name to processs "+pdfFile.getFile().getName());
//                var pdfReader = new ParagraphPdfDocumentReader(pdfFile);
//                vectorStore.accept(textSplitter.apply(pdfReader.get()));
//            }
//        } else {
//            Optional<Resource> file = Arrays.stream(pdfFiles).filter(resource -> resource.getFilename().equalsIgnoreCase(fileName)).findAny();
//            if (file.isPresent()) {
//                var pdfReader = new ParagraphPdfDocumentReader(file.get());
//                vectorStore.accept(textSplitter.apply(pdfReader.get()));
//                log.info("Loaded single file : " + file.get().getFilename());
//            } else {
//                log.info("File Not Found " + file);
//            }
//        }
    }
}