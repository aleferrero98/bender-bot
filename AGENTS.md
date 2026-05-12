# Bender Bot - Project Rules

## Language Conventions

- **Code, comments, logs, exception messages**: MUST be in English
- **Telegram response messages**: MUST be in Spanish (user-facing)

## Class Attribute Order

Attributes in a class MUST be ordered as follows:
1. Constants (`static final`)
2. Static variables
3. Services and clients (injected dependencies)
4. Repositories (injected dependencies)

## Architecture Rules

- System command execution MUST be delegated to `CommandExecutorService`
- Repositories MUST NOT be accessed directly from non-service classes; use a service layer
- All exceptions MUST be properly logged before throwing

## Tunnel Status Values

The `ETunnelStatus` enum has the following values:
- `ACTIVE`: Tunnel is currently running
- `CANCELLED`: Tunnel was manually cancelled by user
- `EXPIRED`: Tunnel expired due to timeout
- `ERROR`: Tunnel encountered an error during creation, cancellation, or expiration

## Database

- MySQL database: `bender_bot`
- Port: 3303 (mapped from container's 3306)
- Tables: `tunnel`, `frequent_app`

## Cache Configuration

- `FrequentAppService` uses Spring Cache with `@Cacheable` and `@CacheEvict`
- Cache name: `frequentApps`
- Cache is automatically evicted every 5 minutes via `@Scheduled`
- Manual cache eviction available via `POST /bender/v1/cache/evict`
