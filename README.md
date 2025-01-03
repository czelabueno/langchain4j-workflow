# JavAI Workflow 🦜🔂☕: Build programmatically custom agentic workflows, AI Agents, RAG systems for java
[![Build Status](https://github.com/czelabueno/langchain4j-workflow/actions/workflows/ci.yaml/badge.svg)](https://github.com/czelabueno/langchain4j-workflow/actions/workflows/ci.yaml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.langchain4j/langchain4j-workflow/badge.svg)](https://maven-badges.herokuapp.com/maven-central/dev.langchain4j/langchain4j-workflow)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

An open-source Java library to build, package, integrate, orchestrate and monitor agentic AI systems for java developers 💡

![Workflow Image](docs/jai-worflow-anatomy.png)
<details>
  <summary>Node, Module, and Workflow Definitions</summary>

### Node
A `Node` represents a single unit of work within the workflow. It encapsulates a specific function or task that processes the stateful bean and updates it. Nodes can be synchronous or asynchronous (streaming).

### Module
A `Module` is a collection of nodes grouped together to perform a higher-level function. Modules can be reused across different workflows, providing modularity and reusability.

### Workflow
A `Workflow` is a directed graph of nodes, modules and edges that defines the sequence of operations to be performed. It manages the state transitions and execution flow, ensuring that each node processes the stateful bean in the correct order.

</details>


> 🌟 **Starring me**: If you find this repository beneficial, don't forget to give it a star! 🌟 It's a simple way to show your appreciation and help this project grow!

## Overview
JavAI Workflow (named initially Langchain4j-workflow) is a dynamic, stateful workflow engine crafted as a Java library. It empowers java developers with granular control over the orchestrated workflows as a graph, iteratively, with cycles, flexibility, control, and conditional decisions. This engine is a game-changer for building sophisticated AI applications, such as multiples RAG-based approaches using modern paradigms and agent architectures. It enables the crafting of custom behavior, leading to a significant reduction in hallucinations and an increase in response reliability.

jAI Workflow is influenced by [LangFlow](https://github.com/langflow-ai/langflow), [LangGraph](https://langchain-ai.github.io/langgraph/tutorials/introduction/), [Graphviz](https://graphviz.gitlab.io/Gallery/directed/).

## Principles:
- **Java-based** jAI workflows are configuration as code (java) and agnostic to AI models, enabling you can define custom advanced and dynamic workflows just writing Java code.
- **Stateful**: jAI Workflow is a stateful engine, enabling you to design custom states as POJO and transitions. This feature provides a robust foundation for managing the flow and state of your application.
- **Graph-Based**: The workflow is graph-based, offering the flexibility to define custom workflows with multiple directions such as one-way, round trip, loop, recursive and more. This feature allows for intricate control over the flow of your application.
- **Flexible**: jAI Workflow is designed with flexibility in mind. You can define custom workflows, modules or agents to build RAG systems as LEGO-like. A module can be decoupled and integrated in any other workflow.
- **Ecosystem integration**: jAI workflow will be integrated with any java AI project. It provides a comprehensive toolset for building advanced java AI applications.
- **Publish as API**: jAI Workflow can be published as an API as entrypoint once you have defined your custom workflow. This feature allows you to expose your workflow as a service.
- **Observability**: jAI Workflow provides observability features to monitor the execution of the workflow, trace inputs and outputs, and debug the flow of your application.
- **Scalable**: jAI Workflow can be deployed as any java project as standalone or distributed mode as containers in any cloud provider or kubernetes environment. Each module can run in a different JVM env or container for scalability in production environments.

## 🚀 Features
### v.0.2.0 Features
- **Graph-core**: The engine supports create `Nodes`, `Conditional Nodes`, `Edges`, and workflows as a graph. This feature allows you to define custom workflows with multiple `Transitions` between nodes such as one-way, round trip and recursive. 
- **Run workflow**: jAI Workflow supports synchronized `workflow.run()` and streaming `worflow.runStream()` runs the outputs as they are produced by each node. This last feature allows for real-time processing and response in your application.
- **Integration**: [LangChain4j](https://docs.langchain4j.dev/) integration, enabling you to define custom workflows using all the features that LangChain4j offers. This integration provides a comprehensive toolset for building advanced AI applications to integrate with multiple LLM providers and models.
- **Visualization**: The engine supports the generation of workflow images. This feature allows you to visualize the flow computed of your app workflow. By Default it uses `Graphhviz` lib to generate the image, but you implement your own image generator on `GraphImageGenerator.java` interface.
### Q1 2025 Features
- **Graph-Core**:
  - Split Nodes
  - Merge Nodes
  - Parallel transitions
  - Human-in-the-loop
- **Modular (Group of nodes)**:
  - Module
  - Remote Module
- **Integration**:
  - Model Context Protocol (MCP) integration as server and client.
  - Define remote module as MCP server.
- **API**:
  - Publish workflow as API (SSE for streaming runs and REST for sync runs).
### 🗺️ Future Features
- **Deployment Model**:
  - Dockerize workflow
  - Kubernetes deployment
  - Cloud deployment
- **Observability**:
  - OpenTelemetry integration (metrics and traces).
  - Debugging mode logging structure.
- **Playground**:
  - Web-based playground to add, test and run jAI workflows APIs.
  - Chatbot Q&A viewer.
  - Graph tracing visualization for debugging
  - 
  ![jai-workflow-playground-prototype](docs/jai-workflow-playground.gif)
  > _This is a prototype, the final version will be available soon. Open an issue if you want to share your ideas or contribute to this feature._

## Architecture
jAI Workflow is designed with a modular architecture, enabling you to define custom workflows, modules, or agents to build RAG systems as LEGO-like. A module can be decoupled and integrated into any other workflow.

### Standalone Architecture
![Standalone Architecture](docs/jai-standalone-architecture.png)

### Distributed Architecture
![Desired Architecture](docs/jai-distributed-architecture.png)

> 📖 Full documentation will be available soon

## 💡How to use in your Java project
In **jAI Workflow**, the notion of state plays a pivotal role. Every execution of the graph initiates a state, which is then transferred among the nodes during their execution. Each node, after its execution, updates this internal state with its own return value. The method by which the graph updates its internal state is determined by user-defined functions.

The simplest way to use jAI Workflow in your project is with the [LangChain4j](https://docs.langchain4j.dev) integration because enables you to define custom workflows using all the features that LangChain4j offers. This integration could provide a comprehensive toolset for building advanced AI applications:
```xml
<dependency>
  <groupId>com.github.czelabueno</groupId>
  <artifactId>jai-workflow-langchain4j</artifactId>
  <version>0.2.0</version> <!--Change to the latest version-->
</dependency>
```

If you would want to use jAI workflow without LangChain4j or with other framework, add the following dependency to your `pom.xml` file:
```xml
<dependency>
  <groupId>com.github.czelabueno</groupId>
  <artifactId>jai-workflow-core</artifactId>
  <version>0.2.0</version> <!--Change to the latest version-->
</dependency>
```
### langChain4j-workflow example
Define a stateful bean with fields that will be used to store the state of the workflow:
```java
// Define a stateful bean
public class MyStatefulBean extends AbstractStatefulBean {
  private List<String> relevantDocuments;
  private String webSearchResponse;
  // other fields you need
}
```
Define functions that determines statefulBean state. To simplify this, you can use a java class with static methods:
```java
public class MyStatefulBeanFunctions {
  public static MyStatefulBean searchWeb(MyStatefulBean statefulBean) {
    // This is a simple example, you can use LangChain4j to search the web using any WebSearchEngine.
    statefulBean.webSearchResponse = "Web search response";
    return statefulBean;
  };
  public static MyStatefulBean extractRelevantDocuments(MyStatefulBean statefulBean, String... uris) {
    // This is a simple example, you can use LangChain4j to extract relevant content of the URIs using any RAG pattern.  
    statefulBean.relevantDocuments = Arrays.asList("Relevant Content 1", "Relevant Content 2");
    return statefulBean;
  };
  public static UserMessage generateUserMessageUsingPrompt(MyStatefulBean statefulBean) {
    return UserMessage.from(answerPrompt(statefulBean).text());
  }
  private static Prompt answerPrompt(MyStatefulBean statefulBean) {
    String question = statefulBean.getQuestion();
    String context = String.join("\n\n", statefulBean.getRelevantDocuments());
    MyStructuredPrompt generateAnswerPrompt = new MyStructuredPrompt(question, context);
    return StructuredPromptProcessor.toPrompt(generateAnswerPrompt);
  }
}
```
Create a simple workflow with 3 nodes and conditional edges:
```java
public class Example {
  public static void main(String[] args) {
    
    MyStatefulBean myStatefulBean = new MyStatefulBean();
    String[] documents = new String[]{
            "https://lilianweng.github.io/posts/2023-06-23-agent/",
            "https://lilianweng.github.io/posts/2023-03-15-prompt-engineering/"
    };
    
    StreamingChatLanguageModel streamingModel = MistralAiStreamingChatModel.builder()
            .apiKey(System.getenv("MISTRAL_AI_API_KEY"))
            .modelName(MistralAiChatModelName.MISTRAL_LARGE_LATEST)
            .temperature(0.0)
            .build();
    
    // Create the nodes and associate them with the functions to be used during execution.
    Node<MyStatefulBean, MyStatefulBean> retrieveNode = Node.from(
            "Retrieve Node", 
            obj -> MyStatefulBeanFunctions.extractRelevantDocuments(obj, documents));
    Node<MyStatefulBean, MyStatefulBean> webSearchNode = Node.from(
            "Web Searching Node", 
            obj -> MyStatefulBeanFunctions.searchWeb(obj));
    StreamingNode<MyStatefulBean> generateAnswerNode = StreamingNode.from(
            "Generation Node",
            obj -> MyStatefulBeanFunctions.generateUserMessageUsingPrompt(obj),
            streamingModel);

    // Create workflow
    DefaultJAiWorkflow<MyStatefulBean> workflow = DefaultJAiWorkflow.<MyStatefulBean>builder()
            .statefulBean(statefulBean)
            .runStream(true)
            .nodes(Arrays.asList(retrieveNode, webSearchNode, generateAnswerNode))
            .build();

    StateWorkflow stateWorkflow = workflow.workflow();
    
    // You can add more nodes after workflow build. E.g. node4
    stateWorkflow.addNode(node4);

    // Define edges
    stateWorkflow.putEdge(retrieveNode, webSearchNode);
    // Conditional edge
    stateWorkflow.putEdge(webSearchNode, Conditional.eval(obj -> {
      if (obj.webSearchResponse != null) {
        return generateAnswerNode;
      } else {
        return retrieveNode;
      }
    }));
    stateWorkflow.putEdge(generateAnswerNode, WorkflowStateName.END);

    // Define which node to start
    stateWorkflow.startNode(retrieveNode);

    // Start conversation with the workflow in streaming mode
    String question = "Summarizes the importance of building agents with LLMs";
    Flux<String> tokens = workflow.answerStream(question);
    tokens.subscribe(System.out::println);

    // Print all computed transitions
    String transitions = stateWorkflow.prettyTransitions();
    System.out.println("Transitions: \n");
    System.out.println(transitions);
  }
}
```
Now you can check the output of the workflow execution.

```shell
STARTING workflow in stream mode..
Processing node: Retrieve Node
Retrieve Node: processed
Processing node: Web Searching Node
Web Searching Node: processed
Processing node: Retrieve Node
Retrieve Node: processed
Processing node: Web Searching Node
Web Searching Node: processed
Processing node: Generation Node
Generation Node: processed
Reached END state
```
The LLM answer will be printed by tokens in the console:
```shell
Building 
agen
ts with 
LLMs 
is 
important 
for three 
key reasons. 
Firstly, 
LLMs serve as 
a powerful 
general problem 
solver, 
extending 
their capabilities 
beyond 
just 
generating text. 
Secondly, they 
act as 
the brain
 of an 
autonomous 
agent system,
 enabling tasks 
like planning 
and task 
decomposition. 
Lastly, 
proof-of-concept 
demos like 
AutoGPT 
and 
BabyAGI 
showcase the
 potential of 
LLM-powered 
agents in 
handling
 complex 
tasks
  efficiently.
```

You can print all computed transitions:

```shell
START -> node1 -> node2 -> node3 -> node2 -> node3 -> node4 -> END
```
You can generate a workflow image with all computed transitions:
```shell
> image/
> ├── my-workflow.svg
```
![Workflow Image](jai-workflow-core/image/my-workflow.svg)

Check the full example in the [langchain4j-worflow tests](https://github.com/czelabueno/jai-workflow/blob/main/jai-workflow-core/src/test/java/com/github/czelabueno/jai/workflow/langchain4j/JAiWorkflowIT.java)

## LLM examples
You can check all examples in the [langchain4j-worflow-examples](https://github.com/czelabueno/langchain4j-workflow-examples) repository where show you how-to implement multiple RAG patterns, agent architectures and AI papers using LangChain4j and jAI Workflow. 

> Please note that examples can be modified and more examples will be added over time.

### MoA
- **Mixture-of-Agents (MoA)**:
  - Java example: [`langchain4j-moa`](https://github.com/czelabueno/langchain4j-workflow-examples/tree/main/langchain4j-moa)
  - Based on Paper: https://arxiv.org/pdf/2406.04692

### RAG
- **Corrective RAG (CRAG)**:
  - Java example: [`langchain4j-corrective-rag`](https://github.com/czelabueno/langchain4j-workflow-examples/tree/main/langchain4j-corrective-rag)
  - Based on Paper: https://arxiv.org/pdf/2401.15884
- **Adaptive RAG**:
  - Java example: _Very soon_
  - Based on Paper: https://arxiv.org/pdf/2403.14403
- **Self RAG**:
  - Java example: _Very soon_
  - Based on Paper: https://arxiv.org/pdf/2310.11511
- **Modular RAG**:
  - Java example: _Very soon_
  - Based on Paper: https://arxiv.org/pdf/2312.10997v1

### Agent Architectures
- **Multi-agent Collaboration**:
  - Java example: _Very soon_
  - Based on Paper: https://arxiv.org/pdf/2308.08155
- **Agent Supervisor**:
  - Java example: _Very soon_
  - Based on Paper: https://arxiv.org/pdf/2308.08155
- **Planning Agents**:
  - Java example: _Very soon_
  - Based on Paper: https://arxiv.org/pdf/2305.04091

## 💬 Contribute & feedback
If you have any feedback, suggestions, or want to contribute, please feel free to open an issue or a pull request. We are open to new ideas and suggestions.
Help us to maturity this project and make it more useful for the java community.

## 🧑🏻‍💻 Authors
- Carlos Zela [@c_zela](https://x.com/c_zela) [czelabueno](https://linkedin.com/in/czelabueno)
