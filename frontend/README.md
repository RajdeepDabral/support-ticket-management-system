# Frontend

React + TypeScript application for the Support Ticket Management System.

## Prerequisites

- Node.js 20+
- Backend running at `http://localhost:8080` (see `../backend/README.md`)

## Setup

```bash
cp .env.example .env
npm install
```

## Development

```bash
npm run dev
```

## Tests

```bash
npm test
```

## Production build

```bash
npm run build
```

## Docker

The frontend is containerized with nginx. Docker Compose builds it with `VITE_API_BASE_URL=/api/v1` and proxies API requests to the backend service.

```bash
docker compose up --build -d
```

Open [http://localhost:3000](http://localhost:3000).

## Configuration

| Variable | Description |
| --- | --- |
| `VITE_API_BASE_URL` | Backend API base URL. Use `http://localhost:8080/api/v1` for host development. Docker builds use `/api/v1` (proxied by nginx). |
