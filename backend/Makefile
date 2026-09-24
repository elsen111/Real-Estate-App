.PHONY: up down restart logs ps build format lint test migrate db-shell backend-shell clean

# ---- Docker lifecycle ----
up:
	docker compose up -d --build

down:
	docker compose down

restart:
	docker compose down && docker compose up -d --build

logs:
	docker compose logs -f backend

ps:
	docker compose ps

build:
	docker compose build backend

clean:
	docker compose down -v --remove-orphans

# ---- Shells ----
backend-shell:
	docker compose exec backend sh

db-shell:
	docker compose exec postgres psql -U $${DB_USERNAME:-postgres} -d $${DB_NAME:-real_estate_db}

# ---- Code quality (run against backend/) ----
format:
	cd backend && ./gradlew spotlessApply

lint:
	cd backend && ./gradlew spotlessCheck

test:
	cd backend && ./gradlew test

migrate:
	cd backend && ./gradlew update