# Bender Bot

A Java-powered Telegram bot to manage a home server — _bite my shiny metal Java code, meatbags!_

## Overview

Bender Bot is a personal Telegram bot designed to remotely manage and monitor a Linux server. It provides a menu-driven interface via inline keyboard buttons for system administration tasks, including:

- **System Info**: Fetch system information via `fastfetch` and hardware temperatures via `lm-sensors`
- **Server Management**: Reboot, shutdown, and control server LEDs
- **Cooler Control**: Set fan speed manually or enable/disable an automatic temperature controller service
- **SSH Tunnels**: Create, list, and cancel Cloudflare tunnels with configurable duration
- **Frequent Apps**: Quickly expose frequently-used applications through Cloudflare tunnels with auto-updating Short.io URLs
- **Backups**: Execute service-specific backups (Immich, Nextcloud, Docker) and cold backups to external disks

## Tech Stack

- **Java 17** with **Spring Boot 4.0.1**
- **TelegramBots** (AbilityBot) 6.7.0
- **MySQL 8** via Spring Data JPA / Hibernate
- **Spring Cache** with scheduled eviction
- **Cloudflare Tunnel** (`cloudflared`) for exposing local services
- **Short.io API** for managing short URLs
- **Lombok** for boilerplate reduction
- **Docker Compose** for the database

## Architecture

```
src/main/java/com/telegram/bender/
├── BenderBotApplication.java       # Spring Boot entry point
├── controller/
│   └── BenderBotController.java    # REST endpoints (health, cache eviction)
├── dto/                            # Data transfer objects
├── model/
│   ├── EBotCommand.java            # Bot command enum (start, info, cooler, etc.)
│   ├── ETunnelStatus.java          # Tunnel lifecycle states
│   ├── EFrequentAppStatus.java     # Frequent app status
│   ├── TunnelEntity.java           # JPA entity for tunnels
│   └── FrequentAppEntity.java      # JPA entity for frequent apps
├── repository/
│   ├── TunnelRepository.java       # Tunnel data access
│   └── FrequentAppRepository.java  # Frequent app data access
└── service/
    ├── BenderBot.java              # AbilityBot: command registration & routing
    ├── ResponseHandler.java        # Handles all bot responses & callback queries
    ├── CommandExecutorService.java # System command execution (fastfetch, sensors, reboot, etc.)
    ├── TunnelService.java          # Tunnel lifecycle management
    ├── FrequentAppService.java     # Frequent app management with caching
    ├── ShortIoClient.java          # Short.io API client
    └── TunnelExpirationScheduler.java # Scheduled tunnel cleanup
```

## Prerequisites

- Java 17+
- Docker & Docker Compose
- `fastfetch`, `lm-sensors`, and `cloudflared` installed on the target server
- A Telegram Bot token (from [@BotFather](https://t.me/BotFather))
- A [Short.io](https://short.io) account with an API key

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/bender-bot.git
cd bender-bot
```

### 2. Configure environment variables

```bash
cp env.example .env
```

Edit `.env` with your actual values. See [Environment Variables](#environment-variables) for details.

### 3. Start the database

```bash
docker compose up -d
```

This starts a MySQL 8 container on port `3303` with the `bender_bot` database.

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The bot starts on port `7071` and registers its commands with Telegram automatically.

## Environment Variables

| Variable | Description | Example             |
|----------|-------------|---------------------|
| `TELEGRAM_BOT_USERNAME` | Bot username (without @) | `my_bot`      |
| `TELEGRAM_BOT_TOKEN` | Bot token from BotFather | `123456:ABC-DEF...` |
| `TELEGRAM_BOT_CREATOR` | Telegram user ID of the bot owner | `987654321`         |
| `MYSQL_ROOT_PASSWORD` | MySQL root password | `secureRootPassword` |
| `MYSQL_USER` | MySQL username | `bender`            |
| `MYSQL_PASSWORD` | MySQL user password | `secureUserPassword` |
| `SHORT_IO_TOKEN` | Short.io API key | `abc123def456`      |
| `SHORT_IO_DOMAIN_ID` | Short.io domain ID | `12345`             |
| `LOGGING_LEVEL_COM_TELEGRAM_BOT` | Log level for bot package (optional) | `INFO`              |
| `LOGGING_LEVEL_ORG_SPRINGFRAMEWORK` | Log level for Spring | `WARN`              |
| `LOGGING_LEVEL_ORG_HIBERNATE_SQL` | Log level for Hibernate SQL | `DEBUG`             |

## Bot Commands

| Command    | Description |
|------------|-------------|
| `/start`   | Start the bot and show welcome message |
| `/info`    | View system information and temperature |
| `/cooler`  | Configure fan speed or temperature controller |
| `/tunnel`  | Manage Cloudflare SSH tunnels |
| `/manage`  | Server management (reboot, shutdown, LEDs) |
| `/backups` | Generate server backups |
| `/help`    | Show available commands |

## REST Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/bender/v1/health` | Health check |
| `POST` | `/bender/v1/frequent-apps/cache/evict` | Manually evict frequent apps cache |

## Database

The application uses MySQL with two main tables:

- **`tunnel`**: Stores active, expired, and cancelled Cloudflare tunnels with their URLs, ports, and expiration times
- **`frequent_app`**: Stores frequently-used applications with their ports and Short.io link IDs

Schema validation is handled by Hibernate (`ddl-auto: validate`).

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

