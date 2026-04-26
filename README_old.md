# 🧠 Spring AI with Ollama – Local Setup Guide

### 🔑 Project Topics
- Ollama Model Integration  
- RAG (Retrieval-Augmented Generation) Application Integration  
- Ollama Embedded Model Prompt Generation  

---

This project demonstrates how to run a **Spring Boot application** that uses **Spring AI** with an **open-source LLM** via **Ollama**, entirely on your local machine – no external APIs required.

---

## 📊 Architecture / Flow Diagram

Below is the high-level flow of how Spring AI integrates with Ollama:

![Flow of RAG](https://raw.githubusercontent.com/smadil997/spring-ai-ollama-angular/master/mis-data/flow-of-rag.png)

---

## ✅ Prerequisites

Make sure the following are installed:

- [Java 17+](https://adoptium.net/)
- [Maven](https://maven.apache.org/) or [Gradle](https://gradle.org/)
- [Ollama](https://ollama.com/download)
- (Optional) [Angular CLI](https://angular.io/cli) and [Node.js](https://nodejs.org/) – if you're using the Angular frontend

---

## 🧰 Step-by-Step Setup

### 1. Install & Set Up Ollama

- Download and install from: [https://ollama.com/download](https://ollama.com/download)
- Open a terminal and run:

  ```bash
  ollama pull llama3
  ollama run llama3
  ```

> ✅ This will start Ollama on `http://localhost:11434`

---

### 2. Set Up the Spring Boot Project

* Use [Spring Initializr](https://start.spring.io/) to generate a Spring Boot project with:

  * Spring Web
  * Spring AI dependencies (add manually if needed)

* In your project:

  * Set the model to use (`llama3`)
  * Set Ollama's base URL (`http://localhost:11434`)
  * Enable auto-model pull if desired

---

### 3. Run the Backend

Make sure Ollama is running, then:

```bash
mvn spring-boot:run
```

> Your backend is now accessible at: `http://localhost:8080`

---

### 4. (Optional) Run Angular Frontend

If you’re using the provided Angular UI:

```bash
cd springai-client
npm install
ng serve
```

> Visit the frontend at: `http://localhost:4200`

---

## 🌐 Local URLs Summary

| Service                     | URL                      |
| --------------------------- | ------------------------ |
| Ollama API                  | `http://localhost:11434` |
| Spring Boot                 | `http://localhost:8080`  |
| Angular Frontend (optional) | `http://localhost:4200`  |

---

## 📌 Notes

* Ollama runs completely offline once the model is downloaded.
* You can switch LLMs easily with `ollama pull <model-name>` and update your config.
* Ideal for experimentation, privacy-focused development, or building local AI tools.
