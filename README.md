# Spring AI RAG implementation and related projects
This project will be the core project where we will be testing various features of the Spring AI framework.
Will will be listening to an AWS SQS queue to consume messages. These will be unpacked and saved into our Vector database.
This will allow us to use RAG to enhance our customer query data.

## Getting Your Development Environment Setup
### Recommended Versions
| Recommended             | Reference                                            | Notes                                                                                                                                                                                                                 |
|-------------------------|------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java 23 JDK             | sdk install java 23-zulu                             | Java 23 will be used in these projects                                                                                                                                                                                |
| IntelliJ 2024 or Higher | [Download](https://www.jetbrains.com/idea/download/) | Ultimate Edition recommended. Students can get a free 120 trial license [here](https://github.com/springframeworkguru/spring5webapp/wiki/Which-IDE-to-Use%3F#how-do-i-get-the-free-120-day-trial-to-intellij-ultimate) |
| Maven 3.9.6 or higher   | [Download](https://maven.apache.org/download.cgi)    | [Installation Instructions](https://maven.apache.org/install.html)                                                                                                                                                    |
| Docker                  |                                                      | [Installation Instructions](https://maven.apache.org/install.html)                                                                                                                                                    |
| Ollama                  | [Download](https://ollama.com/download)              | [Installation Instructions](https://github.com/ollama/ollama)                                                                                                                                                    |
| Weather Service         |              | [Installation Instructions](https://www.weatherapi.com/docs/)                                                                                                                                                    |

## Setup the local environment to run this application

### Start Ollama with local LLM of llama3.2
```shell
 ollama run llama3.2
```
Once the Ollama is running, you can start the Spring Boot application.
Also - in the ~/.ollama/logs folder, you have a server.log file. Open this up and look for the following bits:<br>
```shell
llama_model_loader: - kv   0:                       general.architecture str              = llama
llama_model_loader: - kv   1:                               general.type str              = model
llama_model_loader: - kv   2:                               general.name str              = Llama 3.2 3B Instruct
llama_model_loader: - kv   3:                           general.finetune str              = Instruct
llama_model_loader: - kv   4:                           general.basename str              = Llama-3.2
llama_model_loader: - kv   5:                         general.size_label str              = 3B
llama_model_loader: - kv   6:                               general.tags arr[str,6]       = ["facebook", "meta", "pytorch", "llam...
llama_model_loader: - kv   7:                          general.languages arr[str,8]       = ["en", "de", "fr", "it", "pt", "hi", ...
llama_model_loader: - kv   8:                          llama.block_count u32              = 28
llama_model_loader: - kv   9:                       llama.context_length u32              = 131072
llama_model_loader: - kv  10:                     llama.embedding_length u32              = 3072
```
Here we have the embedding_length. This must be set the same when the VectorDatase properties is set in the application.yaml file, else you will have chunk sizing issues reading
the data again.
Take note that this value is set when the vector_store database table is created!
Another way to get these parameters is to use a crul command:
```shell
curl http://localhost:11434/api/show -d '{
  "name": "llama3.2"
}'
```
Format the response to proper Json and you will find these properties:
```shell
  ],
    "general.type": "model",
    "llama.attention.head_count": 24,
    "llama.attention.head_count_kv": 8,
    "llama.attention.key_length": 128,
    "llama.attention.layer_norm_rms_epsilon": 0.00001,
    "llama.attention.value_length": 128,
    "llama.block_count": 28,
    "llama.context_length": 131072,
    "llama.embedding_length": 3072,
    "llama.feed_forward_length": 8192,
    "llama.rope.dimension_count": 128,
    "llama.rope.freq_base": 500000,
    "llama.vocab_size": 128256,
   
```
Choosing the right model is important. One of the limitations is the chunking size in the PGVector DB. For this we need to have the right embedding model.
In this app we will be using the mxbai-embed-large model. Reference on the following page:
[Ollama Embedding Models](https://ollama.com/blog/embedding-models)

The embedding size is now 1024. This is well withing the 2000 PGVector limit and the model is supported by Ollama.
To pull the model for use in Ollama, use the following command:
```shell
ollama pull mxbai-embed-large
```

You can test the embedding model by using the following command:
```curl http://localhost:11434/api/embeddings -d '{
  "model": "mxbai-embed-large",
  "prompt": "Llamas are members of the camelid family"
}'
```

### Start Open WebUI so we can get the Ollama UI
```shell
docker run -d -p 3000:8080 --add-host=host.docker.internal:host-gateway -v open-webui:/app/backend/data --name open-webui --restart always ghcr.io/open-webui/open-webui:main
```
* [Now start the WebUI on] (http://localhost:3000)

### Start PGVector DB
```shell
docker run -d --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres pgvector/pgvector:0.7.4-pg16
```
Configure the PGVector DB indexes:
[PGVector details](https://tembo.io/blog/vector-indexes-in-pgvector)

We also have a JPA entity that maps onto the vector table. So run the following to initialize the table so JPA is happy.
```sql
create table public.vector_store
(
    id        uuid default uuid_generate_v4() not null primary key,
    content   text,
    metadata  json,
    embedding vector(1024)
);

alter table public.vector_store
    owner to customerai;

create index spring_ai_vector_index
    on public.vector_store using hnsw (embedding public.vector_cosine_ops);
```
### Configure customerai user in PostgreSQL
For this we are simply using local PostgreSQL instance, so not using encrypted passwords and fancy stuff.
<br>The following commands will sort out datasource for us:
```shell
CREATE DATABASE customerai;
CREATE USER customerai WITH PASSWORD 'customerai';
CREATE SCHEMA IF NOT EXISTS customerai AUTHORIZATION customerai;
GRANT ALL PRIVILEGES ON SCHEMA customerai TO customerai;
ALTER ROLE customerai WITH LOGIN;
```
https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html


### Setting up SpringAI with Ollama

### Setting up Functions to be used by the LLM
The first function will use the weather api to retrieve current weather information. This is a free service where we can get an api key to call it.

### Advisors in Spring AI
Spring AI has the concept of advisors. Advisors are used to provide additional information to the LLM.
They are also used to transform the input and output of the LLM. One big use case is for sharing context accross
multiple calls.
<br>
Below is a very good article on advisors:
<br>
[Advisor Implementation](https://spring.io/blog/2024/10/02/supercharging-your-ai-applications-with-spring-ai-advisors)

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.3.4/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.3.4/maven-plugin/build-image.html)
* [Spring Boot Testcontainers support](https://docs.spring.io/spring-boot/3.3.4/reference/testing/testcontainers.html#testing.testcontainers)
* [Testcontainers Postgres Module Reference Guide](https://java.testcontainers.org/modules/databases/postgres/)
* [Testcontainers Ollama Module Reference Guide](https://java.testcontainers.org/modules/testcontainers/)
* [PGvector Vector Database](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)
* [Ollama](https://docs.spring.io/spring-ai/reference/api/clients/ollama-chat.html)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.3.4/reference/htmlsingle/index.html#web)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.3.4/reference/htmlsingle/index.html#data.sql.jpa-and-spring-data)
* [Flyway Migration](https://docs.spring.io/spring-boot/docs/3.3.4/reference/htmlsingle/index.html#howto.data-initialization.migration-tool.flyway)
* [Testcontainers](https://java.testcontainers.org/)


### Testcontainers support

This project uses [Testcontainers at development time](https://docs.spring.io/spring-boot/3.3.4/reference/features/dev-services.html#features.dev-services.testcontainers).

Testcontainers has been configured to use the following Docker images:

* [`ollama/ollama:latest`](https://hub.docker.com/r/ollama/ollama)
* [`pgvector/pgvector:pg16`](https://hub.docker.com/r/pgvector/pgvector)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

