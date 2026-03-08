# CLAUDE.md - JanusGraph Development Guide

## Project Overview

JanusGraph is a highly scalable, distributed graph database optimized for storing and querying graphs with billions of vertices and edges. It is an Apache 2.0 licensed project managed by the LF AI & Data Foundation.

- **Language**: Java 8+ (also tested on Java 11)
- **Build system**: Apache Maven 3.2.5+
- **Graph framework**: Apache TinkerPop 3.7.3 (Gremlin query language)
- **Version**: 1.2.0-SNAPSHOT
- **Group ID**: `org.janusgraph`

## Build Commands

```bash
# Build without tests (most common during development)
mvn clean install -DskipTests=true

# Build with default tests
mvn clean install

# Build a single module (e.g., core)
mvn clean install -pl janusgraph-core -DskipTests=true

# Build a module and its dependencies
mvn clean install -pl janusgraph-core -am -DskipTests=true

# Build with TinkerPop tests
mvn clean install -Dtest.skip.tp=false

# Build distribution archive
mvn clean install -Pjanusgraph-release -Dgpg.skip=true -DskipTests=true

# Regenerate configuration reference docs
mvn --quiet clean install -DskipTests=true -pl janusgraph-doc -am
```

## Testing

Tests use **JUnit 5** with **Mockito** for mocking and **TestContainers** for Docker-based integration tests.

```bash
# Run a single test
mvn test -Dtest=full.or.partial.classname#methodname

# Run a single test in a specific module
mvn test -pl janusgraph-core -Dtest=ClassName#methodName

# Run memory tests (disabled by default)
mvn test -Dtest.skip.mem=false

# Run performance tests (disabled by default)
mvn test -Dtest.skip.perf=false
```

### Backend-specific tests (require Docker)

```bash
# CQL/Cassandra tests
mvn clean install -pl janusgraph-cql -Pcassandra3-murmur

# ScyllaDB tests
mvn clean install -pl janusgraph-cql -Pscylladb

# Elasticsearch tests
mvn clean install -pl janusgraph-es

# Solr tests
mvn clean install -pl janusgraph-solr

# HBase tests
mvn clean install -pl janusgraph-hbase
```

### Test JVM settings
- `-Xms256m -Xmx768m` with HeapDumpOnOutOfMemoryError
- 6-hour timeout per test JVM (surefire configuration)

### Marking flaky tests
```java
// flaky test: https://github.com/JanusGraph/janusgraph/issues/[ISSUE_NUMBER]
@RepeatedIfExceptionsTest(repeats = 3)
public void testFlakyFailsSometimes() {}
```

### Feature-gated tests
```java
@FeatureFlag(feature = JanusGraphFeature.UnorderedScan)
public void testRequiresUnorderedScan() {}
```

## Module Structure

### Core
| Module | Description |
|--------|-------------|
| `janusgraph-core` | Core graph database library: graph operations, schema, transactions, query engine |
| `janusgraph-driver` | Gremlin driver for remote connections |
| `janusgraph-server` | Server components (REST/WebSocket/gRPC) |
| `janusgraph-grpc` | gRPC remote access components |

### Storage Backends
| Module | Description |
|--------|-------------|
| `janusgraph-berkeleyje` | BerkeleyDB Java Edition (local storage) |
| `janusgraph-cql` | Cassandra/ScyllaDB via CQL |
| `janusgraph-hbase` | Apache HBase |
| `janusgraph-scylla` | ScyllaDB-specific backend |
| `janusgraph-bigtable` | Google Cloud Bigtable |
| `janusgraph-inmemory` | In-memory backend (testing) |

### Index Backends
| Module | Description |
|--------|-------------|
| `janusgraph-lucene` | Apache Lucene full-text indexing |
| `janusgraph-es` | Elasticsearch mixed indexing |
| `janusgraph-solr` | Apache Solr mixed indexing |
| `janusgraph-mixed-index-utils` | Shared utilities for mixed index implementations |

### Infrastructure
| Module | Description |
|--------|-------------|
| `janusgraph-backend-testutils` | Shared test utilities and base test classes for backends |
| `janusgraph-test` | Comprehensive test suite |
| `janusgraph-hadoop` | Hadoop/OLAP integration |
| `janusgraph-benchmark` | JMH performance benchmarks |
| `janusgraph-examples` | Example code for various backends |
| `janusgraph-doc` | Documentation generation |
| `janusgraph-all` | Aggregate module (all dependencies) |
| `janusgraph-dist` | Distribution packaging (ZIP/TAR with bundled Cassandra) |

## Code Style and Conventions

### Checkstyle (enforced at build time, `mvn validate`)
- **No star imports** — always use single-class imports
- **No unused imports** — remove redundant imports
- **Import order**: third-party packages, then `java.*`/`javax.*`, then static imports (separated by blank lines)
- **`@Override` required** on all overriding methods
- **One top-level class per file**
- **Modifier order**: `public`, `protected`, `private`, `abstract`, `static`, `final`, `transient`, `volatile`, `synchronized`, `native`, `strictfp`
- **`SimplifyBooleanExpression`** — no redundant boolean logic
- **`StringLiteralEquality`** — use `.equals()`, not `==` for strings
- **`UpperEll`** — use `L` suffix (not `l`) for long literals
- **`ArrayTypeStyle`** — use Java-style arrays (`String[] args`, not `String args[]`)
- **Newline at end of file** for Java files
- **No unnecessary semicolons** after type declarations, enum entries, or try-with-resources

### Formatting (.editorconfig)
- **Indentation**: 4 spaces (Java), 2 spaces (YAML)
- **Charset**: UTF-8
- **No trailing whitespace** in Java files
- **Final newline** in Java and proto files

### License headers
- Apache RAT plugin enforces Apache 2.0 license headers on all source files
- Copyright line: `Copyright [year] JanusGraph Authors`

## Key Source Packages (janusgraph-core)

```
org.janusgraph.core          — Public API (JanusGraph, JanusGraphFactory, schema types)
org.janusgraph.graphdb        — Core graph implementation (database, transactions, types, relations, queries)
org.janusgraph.diskstorage    — Storage backend abstraction layer (Backend, BackendTransaction)
org.janusgraph.util           — Utilities (encoding, stats, data structures)
```

## Contribution Requirements

- **DCO sign-off required**: use `git commit -s` on all commits
- **CLA required**: sign the Contributor License Agreement before first contribution
- **Rebase against master** before submitting PRs
- **Squash to a single commit** for the initial PR submission
- **Update tests** for any code changes
- **Update `docs/configs/janusgraph-cfg.md`** when configuration options change (regenerate via `mvn --quiet clean install -DskipTests=true -pl janusgraph-doc -am`)
- **License compatibility**: new dependencies must be Apache 2.0 compatible; update LICENSE.txt and NOTICE.txt

## CI/CD

GitHub Actions with workflows at `.github/workflows/`:
- **ci-core.yml** — Core module tests (Java 8 & 11, with coverage)
- **ci-backend-cql.yml** — CQL/Cassandra backend tests
- **ci-backend-hbase.yml** — HBase backend tests
- **ci-backend-scylla.yml** — ScyllaDB backend tests
- **ci-index-es.yml** — Elasticsearch index tests
- **ci-index-solr.yml** — Solr index tests
- **ci-benchmark.yml** — JMH benchmarks
- **ci-docs.yml** — Documentation build (MkDocs)
- **lint-proto.yml** — Protobuf linting (buf)

## Key Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Apache TinkerPop | 3.7.3 | Graph traversal framework (Gremlin) |
| SLF4J + Logback | 1.7.36 / 1.2.13 | Logging |
| Dropwizard Metrics | 4.2.27 | Metrics/monitoring |
| Jackson | 2.17.2 | JSON processing |
| Caffeine | 2.9.3 | Caching |
| gRPC + Protobuf | 1.66.0 / 3.25.5 | Remote access |
| TestContainers | 1.20.1 | Docker-based integration tests |
| JUnit 5 | 5.11.0 | Test framework |
| Mockito | 4.11.0 | Mocking |

## Documentation

Documentation uses MkDocs with the Material theme. Source files live in `docs/`.

```bash
# Install dependencies
pip3 install -r requirements.txt

# Build docs
mkdocs build

# Serve locally
mkdocs serve
```
