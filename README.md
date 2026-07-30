# Tweet Audit

Tweet Audit tool, which evaluates tweets against predetermined alignment criteria and generates a list of tweet URLs
marked for manual deletion.

## How It Works

Parse your Twitter archive JSON and convert tweets to CSV and run tweets through Gemini AI in batches, flag problematic
ones, save progress.

Parsing is only done once and subsequent run will only analyze the parse tweets in a configurable batch number.

## Prerequisites

- Java 25
- Maven 3.9+
- Google Gemini API key ([get one here](https://ai.google.dev/))

## Installation

Download and install Java 25 from [Oracle](https://www.oracle.com/java/technologies/downloads/) or
use [SDKMAN](https://sdkman.io/):

```bash
# Using SDKMAN
sdk install java 25-open

# Clone and build
git clone https://github.com/osigie/tweet_audit.git
cd tweet_audit/java
mvn clean package
```

## Configuration

### Environment Variables

```bash
export GEMINI_API_KEY="your-api-key-here"
export X_USERNAME="your_twitter_handle"     
export GEMINI_MODEL="gemini-2.5-flash"       # Default: gemini-2.5-flash
export BATCH_SIZE="10"                       # Default: 10
```

### Criteria Configuration

Create a `config.json` file to define what tweets should be flagged (see `config.example.json`):

```json
{
  "criteria": {
    "forbidden_words": [
      "crypto",
      "NFT"
    ],
    "topics_to_exclude": [
      "Profanity or unprofessional language",
      "Personal attacks or insults",
      "Outdated political opinions"
    ],
    "tone_requirements": [
      "Professional language only",
      "Respectful communication"
    ],
    "additional_instructions": "Flag any content that could harm professional reputation"
  }
}
```

## Usage

### Extract and Analyze Tweets

```bash
java -jar target/tweet_audit-1.0.jar 


# Extracted tweets are stored at `data/source/tweets.json`
# and analyzed tweets are stored at `data/chunks/tweets_*.bin`.
```

What happens:

- Processes tweets in batches (default: 10 per batch)
- Saves progress to `data/checkpoint.txt`
- Writes flagged tweets to `data/analyzed/results.csv`
- Run multiple times to work through your entire archive

The output CSV has two columns:

- `tweet_url`: Full URL to the flagged tweet
- `deleted`: Status flag (starts as `false`, you update it manually after deleting)

### Workflow Example

```bash
# Tweets are extracted from source and analysis starts immediately while using configured batch to load the amount of tweet in memory for analysis
java -jar target/tweet_audit-1.0.jar 

```

## Development

### Running Tests

```bash
# Run all tests
mvn test
```

### Code Quality

```bash
# Format code
mvn spotless:apply

# Check formatting
mvn spotless:check

# Check code style
mvn checkstyle:check

# Full build with all checks
mvn clean verify
```

### Code Standards

This implementation follows:

- Java 25 with modern language features (records, text blocks, switch expressions)
- Builder pattern for complex object construction
- Interface-based design for testability
- Comprehensive error handling and logging
- 2-space indentation (enforced by Spotless)
- Checkstyle for code quality

## Architecture

The implementation uses a feature based architecture:

- **models**: Immutable data classes (Tweet, CheckpointEntry, TweetRecord, TweetWrapper)
- **config**: Configuration loading and settings management
- **io**: File I/O operations (parsing, writing, checkpointing)
- **analysis**: Gemini AI integration with retry and rate limiting
- **application**: Orchestration layer coordinating all components

See [TRADEOFFS.md](TRADEOFFS.md) for detailed architectural decisions and design rationale.

## License

See the root [LICENSE](LICENCE) file for details.
