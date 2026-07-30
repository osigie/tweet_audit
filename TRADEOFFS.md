# Technical Tradeoffs & Design Decisions

## Language Choice: Java

I chose Java for its strong compile-time type safety and mature ecosystem. The static type system catches errors before
runtime, and the JVM provides excellent performance and memory management for batch processing workloads. Modern Java
features (records, text blocks, switch expressions) reduce boilerplate significantly.

The downside is verbosity. Even with modern features, Java requires more code for the same functionality due to explicit
error handling, type declarations, and object construction boilerplate.

## Architecture: Batch Processing with Checkpointing

### Extraction

I chose a sequential (streaming) approach for tweet extraction because this phase only reads the source file and writes
the data into chunk files without performing any computation. This keeps memory usage low regardless of the size of the
input dataset.

Extracted tweets are stored in fixed-size binary chunks, inspired by the write-ahead log (WAL) model. Splitting the
output into chunks prevents a single file from growing indefinitely and makes the analysis phase easier to process one
chunk at a time.

Each write is flushed after it is written to reduce the amount of buffered data that could be lost if the application
terminates unexpectedly. The extraction phase does not implement checkpointing or recovery. If extraction fails partway
through, the partially generated chunks must either be removed and regenerated or recovered manually.

### Analysis

The analysis phase uses batch processing with checkpointing. Only a configurable number of tweets are loaded into memory
at a time, keeping memory usage predictable regardless of the dataset size.

Analysis is intentionally single-threaded. Since a checkpoint is saved after every processed tweet, using a single
thread avoids maintaining multiple thread states, coordinating checkpoints across threads, and handling reconciliation
after failures.

If an error occurs during analysis, processing terminates immediately. Because a checkpoint is persisted after every
successfully processed tweet, the application can resume from the last saved checkpoint instead of reprocessing the
entire dataset.

## Abstraction Layers: Interfaces for Testability

I defined interfaces like (`AnalysisClient`, `FileHandler`) even though only one implementation exists for each. This
enables
testing with mock implementations without requiring a mocking framework.

The tradeoff is added abstraction. For a small tool, this might be over-engineering, but it demonstrates production
patterns: testability through dependency injection and separation of contract from implementation.

## Resilience: Retry and Rate Limiting

On error handling, I configured a timeout for the AI call, and I am terminating for every user errors and retrying for
transient, timeout and resource exhaustion using exponential backoff with jitter while throttling every request to
prevent fast burst on the AI endpoint.

## Error Handling Philosophy

I chose fail-fast error handling. Errors propagate up the call stack wrapped with context. When analysis fails, the
application immediately stops processing and reports the error. The checkpoint doesn't update, so rerunning retries from
that point.

This contrasts with resilient systems that skip failed items and continue. For this workflow, stopping on errors is
safer: issues surface immediately rather than silently skipping tweets. Users can investigate and fix problems (API key,
network, rate limits) before resuming.

## Output Format: Deletion Candidates Only

I write only deletion candidates to the results CSV, not all analyzed tweets. Each row includes the tweet URL and a
`deleted` column initialized to `false`.

This creates a focused action list while minimizing file size. The tradeoff is losing a complete audit trail. If
decisions need review later, the tool must be re-run. For the manual cleanup workflow, the focused approach is more
practical.