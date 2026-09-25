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

## Configuration

| Variable | Description |
| --- | --- |
| `VITE_API_BASE_URL` | Backend API base URL (example: `http://localhost:8080/api/v1`) |
