# LangChain4j Workflow 🦜🔀

Build advanced java applications with AI based on flexible stateful workflows 💡

## Overview
LangChain4j Workflow is a dynamic, stateful workflow engine crafted as a Java library, drawing inspiration from graph network libraries. It empowers developers with granular control over the flow and state of their applications. This engine is a game-changer for building sophisticated AI applications, such as RAG-based approaches using modern paradigms and agent architectures, where the application's flow and state are pivotal. It enables the crafting of custom behavior, leading to a significant reduction in hallucinations and an increase in response reliability

LangChain4j Workflow is influenced by [LangGraph](https://langchain-ai.github.io/langgraph/tutorials/introduction/), [Graphviz](https://graphviz.gitlab.io/Gallery/directed/) and [Apache Beam](https://beam.apache.org/), and it offers a multitude of benefits. It allows you to define custom workflows as a graph, iteratively, with cycles, flexibility, control, and conditional decisions. These benefits are indispensable for building advanced AI applications.

LangChain4j Workflow is designed to integrate seamlessly with [LangChain4j](https://docs.langchain4j.dev/), enabling you to define custom workflows using all the features that LangChain4j offers. This integration could provide a comprehensive toolset for building advanced AI applications.

> **Give me a star**: If you find this repository beneficial, don't forget to give it a star! 🌟 Your support increases its chances of being merged with the LangChain4j codebase. It's a simple way to show your appreciation and help this project grow!

## Key Features
- **Stateful**: LangChain4j Workflow is a stateful engine, enabling you to design custom states as POJO and transitions. This feature provides a robust foundation for managing the flow and state of your application.
- **Graph-Based**: The workflow is graph-based, offering the flexibility to define custom workflows with multiple directions such as one-way, round trip, cyclic, and more. This feature allows for intricate control over the flow of your application.
- **Flexible**: LangChain4j Workflow is designed with flexibility in mind. You can define custom workflows and append them at any point in other RAG paradigms, such as Modular RAG. This flexibility allows for a high degree of customization.
- **Iterative**: The engine supports the implementation of loops and conditionals in your custom workflows. This feature allows for complex logic and flow control within your workflows.
- **Streaming Support**: LangChain4j Workflow supports streaming outputs as they are produced by each node. This feature allows for real-time processing and response in your application.
- **Integration**: LangChain4j Workflow is designed to integrate seamlessly with [LangChain4j](https://docs.langchain4j.dev/), enabling you to define custom workflows using all the features that LangChain4j offers. This integration provides a comprehensive toolset for building advanced AI applications.

## Installation
```shell
mvn clean package install
```
## Example
In **LangChain4j Workflow**, the notion of state plays a pivotal role. Every execution of the graph initiates a state, which is then transferred among the nodes during their execution. Each node, after its execution, updates this internal state with its own return value. The method by which the graph updates its internal state is determined by user-defined functions.

Add the following dependency to your `pom.xml` file:
```xml
<dependency>
  <groupId>dev.langchain4j</groupId>
  <artifactId>langchain4j-workflow</artifactId>
  <version>0.1.0</version> <!--Change to the latest version-->
</dependency>
```
Define a stateful bean with fields that will be used to store the state of the workflow:
```java
// Define a stateful bean
public class MyStatefulBean {
  int value = 0;
}
```

Create a simple workflow with 4 nodes and conditional edges:
```java
public class Example {
  public static void main(String[] args) {
    
    MyStatefulBean myStatefulBean = new MyStatefulBean();

    // Define functions that determines statefulBean state
    Function<MyStatefulBean, String> node1Func = obj -> {
      obj.value +=1;
      System.out.println("Node 1: [" + obj.value + "]");
      return "Node1: function proceed";
    };
    Function<MyStatefulBean, String> node2Func = obj -> {
      obj.value +=2;
      System.out.println("Node 2: [" + obj.value + "]");
      return "Node2: function proceed";
    };
    Function<MyStatefulBean, String> node3Func = obj -> {
      obj.value +=3;
      System.out.println("Node 3: [" + obj.value + "]");
      return "Node3: function proceed";
    };
    Function<MyStatefulBean, String> node4Func = obj -> {
      obj.value +=4;
      System.out.println("Node 4: [" + obj.value + "]");
      return "Node4: function proceed";
    };

    // Create the nodes and associate them with the functions to be used during execution.
    Node<MyStatefulBean, String> node1 = Node.from("node1", node1Func);
    Node<MyStatefulBean, String> node2 = Node.from("node2", node2Func);
    Node<MyStatefulBean, String> node3 = Node.from("node3", node3Func);
    Node<MyStatefulBean, String> node4 = Node.from("node4", node4Func);


    // Create workflow
    StateWorkflow<MyStatefulBean> workflow = DefaultStateWorkflow.<MyStatefulBean>builder() 
            .statefulBean(myStatefulBean)
            .addNodes(Arrays.asList(node1, node2, node3))
            .build();

    // You can add more nodes after workflow build. E.g. node4
    workflow.addNode(node4);

    // Define edges
    workflow.putEdge(node1, node2);
    workflow.putEdge(node2, node3);
    // Conditional edge
    workflow.putEdge(node3, Conditional.eval(obj -> {
      System.out.println("Stateful Value [" + obj.value + "]");
      if (obj.value > 6) {
        return node4;
      } else {
        return node2;
      }
    }));
    workflow.putEdge(node4, WorkflowStateName.END);

    // Define which node to start
    workflow.startNode(node1);

    // Run workflow normally
    workflow.run();
    // OR
    // Run workflow in streaming mode
    workflow.runStream(node -> {
      System.out.println("Processing node: " + node.getName());
    });

    // Print all computed transitions
    String transitions = workflow.prettyTransitions();
    System.out.println("Transitions: \n");
    System.out.println(transitions);

    // Generate workflow image
    workflow.generateWorkflowImage("image/my-workflow.svg");
    // workflow.generateWorkflowImage(); // if you use this method, it'll use by default the root path and default image name.
  }
}
```
Now you can check the output of the workflow execution.

```shell
STARTING workflow in stream mode..
Processing node: node1
Node 1: [1]
Processing node: node2
Node 2: [3]
Processing node: node3
Node 3: [6]
Stateful Value [6]
Processing node: node2
Node 2: [8]
Processing node: node3
Node 3: [11]
Stateful Value [11]
Processing node: node4
Node 4: [15]
Reached END state
```
You can print all computed transitions:

```shell
START -> node1 -> node2 -> node3 -> node2 -> node3 -> node4 -> END
```
You can generate a workflow image with all computed transitions:

![Workflow Image](image/my-workflow.svg)

## LLM examples
You can check all examples in the [langchain4j-worflow-examples](https://github.com/czelabueno/langchain4j-workflow-examples) repository. Please note that examples can be modified and more examples will be added over time.

### RAG
- **Corrective RAG (CRAG)**:
  - Java example: [`langchain4j-corrective-rag`](https://github.com/czelabueno/langchain4j-workflow-examples/langchain4j-corrective-rag/src/test/java/dev/langchain4j/rag/corrective/CorrectiveRagIT.java)
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

## Contribute & feedback
If you have any feedback, suggestions, or want to contribute, please feel free to open an issue or a pull request. We are open to new ideas and suggestions.
Help us to maturity this project and make it more useful for the community in order to merge it with LangChain4j source code.

## Authors
- Carlos Zela [@c_zela](https://x.com/c_zela)
