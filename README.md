# Distributed Rate Limiter

A distributed rate limiter built using Java, Spring Boot, Redis, and Redisson.

The project supports multiple rate-limiting algorithms behind a common strategy-based architecture. Redis is used for shared client configuration and rate-limit state, while Redisson distributed locks ensure correct state updates when multiple requests access the same client concurrently.

## Features

- Supports four rate-limiting algorithms:
    - Fixed Window
    - Sliding Window
    - Token Bucket
    - Leaky Bucket
- Redis-backed client configuration and rate-limit state
- Distributed locking using Redisson
- Thread-safe read → update → write state transitions
- Runtime policy configuration per client
- Policy validation
- Strategy and Factory design patterns
- Centralized exception handling
- Standardized API error responses
- Redis failure handling with HTTP `503 Service Unavailable`
- Rate-limit violations with HTTP `429 Too Many Requests`
- Request validation using Jakarta Bean Validation
- Unit, concurrency, and integration testing

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 3.5 | Application framework |
| Spring Web | REST APIs |
| Spring Data Redis | Redis interaction |
| Redisson | Distributed locking and Redis connection support |
| Redis | Distributed state and configuration storage |
| Lombok | Boilerplate reduction |
| Maven | Build and dependency management |
| JUnit / Spring Boot Test | Testing |

## Architecture

The application follows a strategy-based architecture that allows different rate-limiting algorithms to be selected dynamically based on the client's configured policy.

```text
                    ┌─────────────────────┐
                    │      REST Client    │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │RateLimiterController│
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │  RateLimiterService │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
       PolicyFactory     StrategyFactory   StateRepositoryFactory
              │                │                │
              ▼                ▼                ▼
       RateLimitPolicy   RateLimiterStrategy   StateRepository
                               │                │
                               ▼                ▼
                         Algorithm Logic       Redis