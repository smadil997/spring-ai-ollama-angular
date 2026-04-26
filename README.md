# 🧠 Deep Dive into Embeddings: Practical Implementation of RAG with Spring Boot AI

<div align="center">

![Complete Application View](https://storage.googleapis.com/mohdadil-dev-blog/blog-images/deep-diveiinto-embeddings-practical-implementation-with-spring-boot-aI-ollama-and-vector-databases/blog-images/complete-application-view.png)

*Photo by [mohdadil.dev](https://www.mohdadil.dev)*

</div>

---

In this blog, we'll explore embeddings and their practical implementation using **Spring Boot AI**, **Ollama**, and **vector databases**. We'll dive deep into each concept to understand how the process works internally and how the overall system operates in real-world applications.

While it may not be possible to cover every detail in a single post, this guide will walk you through the most important concepts and hands-on implementation. We'll continue this topic as a series in future posts, exploring each component in greater depth.

---

## 📚 Series Contents

| # | Title | Status |
|---|-------|--------|
| 1 | [Building an ETL Pipeline for RAG Applications using Spring AI, Qdrant, and Ollama](https://www.mohdadil.dev/blog/building-an-etl-pipeline-for-rag-applications-using-spring-ai-qdrant-and-ollama) | ✅ Published |
| 2 | **Deep Dive into Embeddings: Practical Implementation with Spring Boot AI** | ✅ You are here |
| 3 | Deep Dive into Tokenization: Practical Implementation with Spring Boot AI | 🔜 Coming Soon |
| 4 | Deep Dive into How LLM will work with Embeddings | 🔜 Coming Soon |

> **NOTE:** Code will be pushed to the repository soon.

---

> ### 🔍 High-level overview
> This is a high-level overview of how the **Retrieval-Augmented Generation (RAG)** process works with an embedding model in a Spring Boot application.

---

## 🔷 Let's Explore Embeddings

Embeddings work by converting text, images, and videos into arrays of floating-point numbers like `[0.016070, 0.024130, -0.012603, 0.002026, 0.001035, -0.003286]`, called **vectors**. These vectors are designed to capture the **semantic meaning** of the input data. The length of an embedding array is known as its **dimensionality**.

By calculating the numerical distance between the vector representations of two inputs, an application can determine how similar those inputs are.

This is a high-level overview of how our Spring Boot AI application works and how RAG can be implemented using Spring Boot AI and Ollama. We will dive deeper into each component in the upcoming sections.

<!-- Architecture SVG Diagram -->
<div align="center">

```
┌─────────────────────────────────────────────────────────────────────┐
│          Spring Boot AI + Ollama · Local Machine Architecture        │
├──────────────────────────────┬──────────────────────────────────────┤
│  EMBEDDING FLOW: PDF→Vectors │    CHAT FLOW: Query→Response         │
│                              │                                      │
│  [PDF Upload]                │              [User Query]            │
│       ↓                      │                   ↓                  │
│  [Document Reader]           │           [Embed Query]              │
│  Apache Tika · extract text  │         qwen3-embedding:8b           │
│       ↓ raw text             │                   ↓                  │
│  [Text Extraction]           │           [Query Vector]             │
│  clean text from PDF pages   │        [0.11, -0.43, 0.85, ...]     │
│       ↓ full text            │                   │                  │
│  [Chunking]                  │         semantic search ↓            │
│  split into small pieces     │                   │                  │
│  ch.1 | ch.2 | ch.3 | ...    │    ┌──────────────────────────┐     │
│       ↓                      │    │      Vector Database      │     │
│  [Embedding Generation] ─────┼───▶│   pgvector / Chroma       │     │
│  qwen3-embedding:8b          │    │  [0.12, -0.45, 0.87]      │     │
│  [0.12, -0.45, 0.87, ...]    │    │  [0.34,  0.11, -0.92]     │     │
│                              │    │  [0.08, -0.67,  0.45]     │     │
│  ← same model used ──────────┼────┘           │               │     │
│                              │           top-K chunks ↓        │     │
│                              │           [Top-K Chunks]         │     │
│                              │        most relevant results     │     │
│                              │                   ↓              │     │
│                              │        [Context Injection]       │     │
│                              │        chunks injected to prompt │     │
│                              │                   ↓              │     │
│                              │         [qwen3:8b (LLM)]         │     │
│                              │      generates response·Ollama   │     │
│                              │                   ↓              │     │
│                              │          [LLM Response]          │     │
│                              │     grounded answer to user      │     │
└──────────────────────────────┴──────────────────────────────────────┘

Legend:
● Embedding model: qwen3-embedding:8b  (shared by both flows)
● Chat LLM: qwen3:8b · served via Ollama locally
```

</div>

---

## 🔢 Embedding Process Step by Step

### 1️⃣ Internal Working — Tokenization with `qwen3-embedding:8b` Model

Tokenization is the process of breaking down text into smaller units called **tokens**. These tokens can be words, subwords, or even characters, depending on the tokenization strategy used. In the context of embedding models, tokenization is the first step in converting text into a format that can be processed by the model.

![Tokenization Step](https://storage.googleapis.com/mohdadil-dev-blog/blog-images/deep-diveiinto-embeddings-practical-implementation-with-spring-boot-aI-ollama-and-vector-databases/blog-images/how-embedding-flow-work-step5-part1.png)

---

### 2️⃣ Now Token will create a Vector

After this process each token is converted into a **numerical vector** that represents its semantic meaning.

![Token to Vector](https://storage.googleapis.com/mohdadil-dev-blog/blog-images/deep-diveiinto-embeddings-practical-implementation-with-spring-boot-aI-ollama-and-vector-databases/blog-images/how-embedding-flow-work-step5-part2.png)

---

### 3️⃣ Transformation Encoding

In embedding models, vectors pass through **multiple layers** where various transformations are applied to create more meaningful representations.

![Transformation Encoding](https://storage.googleapis.com/mohdadil-dev-blog/blog-images/deep-diveiinto-embeddings-practical-implementation-with-spring-boot-aI-ollama-and-vector-databases/blog-images/how-embedding-flow-work-step5-part3.png)

---

### 4️⃣ Token Contextualized

In this step, the tokens are **contextualized**, meaning their representation is influenced by the surrounding tokens in the sentence.

> 💡 **Contextualized meaning:** The meaning of a word depends on the sentence around it.
>
> **Example:** *"The bank is on the river"* vs *"I need to go to the bank to withdraw money."*
> In the first sentence, "bank" refers to the side of a river, while in the second it refers to a financial institution. The embedding model captures these contextual differences and generates **different vectors** for "bank" in each sentence.

![Token Contextualization](https://storage.googleapis.com/mohdadil-dev-blog/blog-images/deep-diveiinto-embeddings-practical-implementation-with-spring-boot-aI-ollama-and-vector-databases/blog-images/how-embedding-flow-work-step5-part4.png)

---

### 5️⃣ Pooling Algorithm

**Pooling = combining many pieces of information into one summary.** In the Pooling Algorithm, the individual token embeddings are combined to create a **single vector representation** for the entire sequence.

**Example:**

For the sentence *"I love ice cream"*, during vectorization:

| Token  | Vector                     |
|--------|----------------------------|
| I      | `[0.12, -0.45, 0.87]`     |
| love   | `[0.34,  0.11, -0.92]`    |
| ice    | `[0.08, -0.67,  0.45]`    |
| cream  | `[0.56,  0.23, -0.78]`    |
| **Pooled** | **`[0.25, -0.33, 0.12]`** |

The pooled vector captures the overall meaning of the sentence and can be used for similarity search or classification.

![Pooling Algorithm](https://storage.googleapis.com/mohdadil-dev-blog/blog-images/deep-diveiinto-embeddings-practical-implementation-with-spring-boot-aI-ollama-and-vector-databases/blog-images/how-embedding-flow-work-step5-part5.png)

---

### 6️⃣ Final Embedding Output

After the pooling algorithm combines the token embeddings, the **final embedding output** is a single vector that represents the entire sequence. This vector can be used for downstream tasks such as similarity search or classification.

![Final Embedding Output](https://storage.googleapis.com/mohdadil-dev-blog/blog-images/deep-diveiinto-embeddings-practical-implementation-with-spring-boot-aI-ollama-and-vector-databases/blog-images/how-embedding-flow-work-step5-part6.png)

---

## 📊 2D Visualization — How Embedding Data Maps into Vector Space

In the image below, we use simple sentences like *"Delhi is the capital of India,"* *"India is a country in Asia,"* and *"Elephant is a large animal."*

When we pass this text to our embedding model in Ollama (`qwen3-embedding:8b`), the model converts the text into vectors and stores them in a vector database like Qdrant.

Later, when a user asks *"What is the capital of India?"*, the query is also converted into a vector. This vector is compared with stored vectors to find the top-K most similar results. These relevant results are combined with the user's query and sent to the LLM (`qwen3:8b`).

Finally, the LLM generates a response based on both the prompt and the retrieved context from the vector database. This is how embeddings work in practice using Spring Boot AI, Ollama, and a vector database.

<div align="center">

![2D Vector Space Visualization](https://storage.googleapis.com/mohdadil-dev-blog/blog-images/deep-diveiinto-embeddings-practical-implementation-with-spring-boot-aI-ollama-and-vector-databases/blog-images/visualize-word-embeddings1.png)

*Photo by [mohdadil.dev](https://www.mohdadil.dev)*

</div>

---

## 📖 Traditional Embedding Techniques

Almost every embedding technique depends on a large corpus of text to understand relationships between words. Earlier approaches relied on **statistical methods**, focusing on how often words appear and co-appear in text. The idea was simple: if two words frequently show up together, they are likely related in meaning.

### TF-IDF (Term Frequency–Inverse Document Frequency)

The idea of TF-IDF is to calculate the importance of a word in a document by considering two factors:

#### 1️⃣ Term Frequency (TF)
TF measures how frequently a word appears in a document. The more times a word appears, the higher its TF score. However, this can lead to common words like *"the"* or *"is"* having high TF scores, which is not ideal.

#### 2️⃣ Inverse Document Frequency (IDF)
IDF measures how important a word is across a collection of documents. Words that appear in many documents have a lower IDF score, while rare words have a higher IDF score. This helps to down-weight common words and highlight unique terms.

<div align="center">

![TF-IDF Flow and Calculation](https://storage.googleapis.com/mohdadil-dev-blog/blog-images/deep-diveiinto-embeddings-practical-implementation-with-spring-boot-aI-ollama-and-vector-databases/blog-images/idf-flow-and0calculation.png)

*Photo by [mohdadil.dev](https://www.mohdadil.dev)*

</div>

---

## 🔗 How Does Embedding Fit into an LLM? (How RAG Answers Your Query)

> Embeddings do **NOT** correct sentences. They only convert text → vectors (for similarity search).

The complete flow of how an embedding model fits with an LLM in a RAG system:

<div align="center">

![How Embedding Fits with LLM](https://storage.googleapis.com/mohdadil-dev-blog/blog-images/deep-diveiinto-embeddings-practical-implementation-with-spring-boot-aI-ollama-and-vector-databases/blog-images/how-embedding-fit-with-llm.png)

*Photo by [mohdadil.dev](https://www.mohdadil.dev)*

</div>

---

## 💻 Practical Examples with Code

Before we dive into the code, let's quickly recap what we've covered so far. We've discussed the ETL process in Spring Boot AI, explored how embedding models work, and visualized how text is transformed into vectors and stored in a vector database. Now, let's see how this works in practice.

### 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| **Java 17 + Spring Boot AI** | Application framework with AI integration |
| **Ollama + `qwen3-embedding:8b`** | Local embedding model to convert text → vectors |
| **Qdrant** | Vector database to store and retrieve embeddings |

---

### 🔹 Calling Prompt API Without Embedding

First, I'll simply send a prompt to the LLM (`qwen3:8b`) and see how it answers `"delhi is a "` **without any context** from a vector database.

```
GET http://localhost:8080/api/embedding/chat/{prompt}
GET http://localhost:8080/api/embedding/chat/delhi is a
```

<div align="center">

![First Query Result](https://storage.googleapis.com/mohdadil-dev-blog/blog-images/deep-diveiinto-embeddings-practical-implementation-with-spring-boot-aI-ollama-and-vector-databases/blog-images/first-query-result-with-expla.png)

*Photo by [mohdadil.dev](https://www.mohdadil.dev)*

</div>

As you can see, I've added logs to the application console to show what happens internally when a query is sent through the controller. You can observe the complete flow, including how the `QuestionAnswerAdvisor` interacts with the chat model during the process.

```java
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
    // ─────────────────────────────────────────────
    log.info("🔢 STEP 2 — CONVERTING QUERY TO EMBEDDING VECTOR...");
    float[] queryEmbedding = embeddingModel.embed(userText);

    log.info("   Model used     : {}", embeddingModel.getClass().getSimpleName());
    log.info("   Vector dims    : {}", queryEmbedding.length);
    log.info("   First 10 values: {}", Arrays.toString(Arrays.copyOf(queryEmbedding, 10)));

    // ─────────────────────────────────────────────
    // STEP 3: Vector search
    // ─────────────────────────────────────────────
    List<Document> similarDocs = vectorStore.similaritySearch(
        SearchRequest.builder()
            .query(userText)
            .topK(4)
            .similarityThreshold(0.5)
            .build()
    );

    for (int i = 0; i < similarDocs.size(); i++) {
        Document doc = similarDocs.get(i);
        String preview = doc.getText().length() > 120
            ? doc.getText().substring(0, 120) + "..."
            : doc.getText();

        log.info("Chunk #{}: {}", i + 1, preview);
    }

    // ─────────────────────────────────────────────
    // STEP 5: Call LLM
    // ─────────────────────────────────────────────
    QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);

    ChatResponse chatResponse = ChatClient.builder(chatModel)
        .build()
        .prompt()
        .advisors(questionAnswerAdvisor)
        .user(userText)
        .call()
        .chatResponse();

    return chatResponse.getResult().getOutput().getText();
}
```

---

### 🔸 Calling Prompt API With Embedding

Before calling the prompt API, I first created three text files and injected them into the vector database through an API:

| File | Content |
|---|---|
| `elephant is a large animal.txt` | `elephant is a large animal` |
| `India is country in asia.txt` | `India is country in asia` |
| `Delhi is a capital of India.txt` | `Delhi is a capital of India` |

I created an `ETLProcessingService` to process the files. This service includes a `processETL` method that accepts `MultipartFile` inputs for processing. Logs and comments are added to make debugging and monitoring easier.

```java
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

        for (int i = 0; i < chunks.size(); i++) {

            Document data = chunks.get(i);

            log.info("------------ CHUNK {} ------------", i);
            log.info("Metadata: {}", data.getMetadata());

            String textPreview = data.getText().substring(0, Math.min(200, data.getText().length()));
            log.info("Text Preview: {}", textPreview);

            int approxTokens = data.getText().length() / 4;
            log.info("Approx Token Count: {}", approxTokens);

            EmbeddingResponse response = embeddingModel.embedForResponse(
                List.of(data.getText())
            );

            float[] vector = response.getResults().get(0).getOutput();

            log.info("Embedding vector size: {}", vector.length);
            log.info("Embedding sample: [{}, {}, {}, {} ...]",
                vector[0], vector[1], vector[2], vector[3]);
        }

        vectorStore.add(chunks);

        log.info("ETL process completed successfully!");

    } catch (Exception e) {
        log.error("Exception occurred during ETL process", e);
    }
}
```

Now, after generating the embeddings, we call the same prompt API. The prompt remains the same: **"Delhi is a"**.

The response is:

> *"Delhi is a **capital**. The context indicates that "Del" (likely part of "Delhi") is associated with "capital," and the repeated "is" suggests the structure "Delhi is a [something]." Thus, the most fitting completion is "capital.""*

As you can see, we have successfully generated an embedding for the input text and used it to retrieve relevant information from the vector database.

Now you can understand how embeddings work in the **Retrieval-Augmented Generation (RAG)** process.

---

## 🔗 Resources

- 📦 **GitHub Repository:** [spring-ai-ollama-angular](https://github.com/smadil997/spring-ai-ollama-angular)
- 🌐 **Blog:** [mohdadil.dev](https://www.mohdadil.dev)

---

> **NOTE:** In the next part, I will cover how chunks were created in detail.

---

<div align="center">

*• • • • •*

**Part of the Spring Boot AI + RAG Series**

[⬅️ Part 1: Building an ETL Pipeline](https://www.mohdadil.dev/blog/building-an-etl-pipeline-for-rag-applications-using-spring-ai-qdrant-and-ollama) | Part 3: Tokenization *(Coming Soon)*

</div>