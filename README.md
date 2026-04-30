# Spatial Index Visualizer

> A visualization of spatial indexing data structures using the Processing libraries.

---

## What is this?

This project demonstrates how various spatial data structures organize and store spatial information.
It uses the Processing library to create interactive visualizations of different spatial indexing
algorithms including Quadtree, K-D tree, and R-tree data structures.

---

## Example Visualizations

### Quadtree
![Quadtree](./output/QuadTree-0001.png)

### K-D tree
![K-D tree](./output/KDTree-0001.png)

### R-tree
![R-tree](./output/RTree-0001.png)

---

## Prerequisites

Before you try to build or run the project, make sure you have:

- Java Development Kit (JDK) 25
- Apache Maven 3.9 or newer

The project targets Java 25 in `pom.xml`, so older JDK releases will not work.

---

## Quick Start

To get started, clone this repository and `cd` to the project root:

```bash
git clone <repository-url>
cd spatial-index-visualizer
```

### Basic Maven commands

Build (compile + package):

```bash
mvn clean package
```

Run via Maven (calls the `main` class):

```bash
mvn exec:java
```

Run the packaged JAR produced by the build:

```bash
java -jar target/spatial-index-visualizer.jar
```

Clean build artifacts:

```bash
mvn clean
```

---