# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LABIT (RABIT) is an AIoT (AI + IoT) education platform that guides students through building Arduino-based hardware projects with AI assistance. The platform consists of a **client-side vanilla JavaScript SPA** and a **Node.js/Express backend** with MongoDB.

### Multi-Step Learning Flow

The platform guides users through 6 sequential steps:
1. **Step 1: Kit Selection** - Choose from pre-defined Arduino kits (smart insole, smart piano, etc.)
2. **Step 2: Brainstorming** - Interactive mind-mapping with drag-and-drop sticky notes (A/B/C sections)
3. **Step 3: Hardware Design** - Select components and configure Arduino connections using Web Serial API
4. **Step 4: Coding** - Answer questions to generate Arduino code line-by-line using Ollama AI
5. **Step 5: AI Tutor** - Explain code, monitor serial output, and upload to Arduino
6. **Step 6: Data Upload** - Upload sensor data to Google Sheets

Each step stores its state in `localStorage` and the MongoDB `Project` model, allowing users to resume progress.

## Commands

### Server (Backend)
```bash
# From server/ directory
npm run dev          # Start with nodemon (auto-restart)
npm start            # Start production server
npm run seed:kits    # Seed kit database with test data
npm run seed:hardware # Seed hardware components data
```

### Client (Frontend)
The client is a static SPA. Serve via:
- Live Server extension in VS Code
- `python3 -m http.server 8080` from client/ directory
- Any static file server on port 8080

### Environment Setup
1. Copy `server/.env.example` to `server/.env`
2. Required environment variables:
   - `MONGODB_URI` - MongoDB connection string
   - `JWT_SECRET` - Secret for JWT token signing
   - `OLLAMA_URL` - Ollama API endpoint (default: http://localhost:11434)
   - `OLLAMA_MODEL` - Model name (default: codellama:7b)
   - `CLIENT_URL` - Frontend URL for CORS (default: http://localhost:8080)

## Architecture

### Backend Architecture (server/)

**MVC Pattern with ES Modules:**
- `server.js` - Entry point, Express setup, MongoDB connection
- `routes/` - Express route definitions (auth, kits, projects, ai)
- `controllers/` - Request handlers and business logic
- `models/` - Mongoose schemas (User, Kit, Project)
- `middleware/` - JWT auth (`protect`), error handling
- `services/` - External integrations (Ollama AI service)
- `scripts/` - Database seeding utilities

**Key Models:**
- `Project` - Stores user progress across all steps (brainstorming, hardwareDesign, code, generatedCode, status)
- `Kit` - Pre-defined Arduino kits with components, sensors, and learning objectives
- `User` - Authentication with bcrypt password hashing and JWT

**Authentication Flow:**
- JWT tokens stored in `localStorage` on client
- `protect` middleware validates Bearer tokens on protected routes
- Token expiry: 7 days (configurable via `JWT_EXPIRE`)

### Frontend Architecture (client/)

**Vanilla JavaScript SPA with Client-Side Routing:**
- `client/index.html` - Entry point
- `client/pages/*.html` - Step pages (login, step1-6)
- `client/js/pages/*.js` - Page controllers (one per step, class-based)
- `client/js/utils/` - Shared utilities:
  - `api.js` - API client with auth headers
  - `auth.js` - Auth helper (token management, user info)
  - `storage.js` - localStorage wrapper
  - `serial.js` - **Web Serial API wrapper for Arduino communication**
  - `dom.js` - DOM utilities (showAlert, etc.)
- `client/js/router.js` - Basic pathname-based routing
- `client/js/app.js` - Legacy routing (may be unused)

**Page Controller Pattern:**
Each step page follows this structure:
```javascript
class StepXPage {
    constructor() {
        // Initialize state from localStorage/API
        // Get DOM references
        this.init();
    }

    async init() {
        // Auth check
        // Load data from API
        // Setup event listeners
    }

    // Event handlers and business logic
}

export default function initStepX() {
    new StepXPage();
}
```

### Web Serial API Integration (Arduino Communication)

**Critical:** The platform uses the **Web Serial API** for direct browser-to-Arduino communication.

`SerialPortManager` class (`client/js/utils/serial.js`):
- `requestPort()` - Prompts user to select Arduino port
- `connect(baudRate)` - Opens connection (default 9600 baud)
- `startReading(callback)` - Reads serial data stream
- `write(data)` - Sends commands to Arduino
- `disconnect()` - Closes port

**Browser Requirements:** Chrome/Edge only (Firefox does not support Web Serial API)

**Usage in Step 3 & 5:**
- Step 3: Connect to Arduino to verify hardware connections
- Step 5: Monitor real-time serial output for debugging

### AI Code Generation Flow (Ollama)

**Step 4 Architecture:**
1. User answers questions about their project (sensors, outputs, logic)
2. Each answer generates 1-2 lines of Arduino code via `ollamaService.generateCodeLine()`
3. All code lines are collected and finalized via `ollamaService.finalizeCode()`
4. Complete Arduino program is saved to `Project.generatedCode`

**Key Service:** `server/services/ollamaService.js`
- Uses Ollama API (local LLM server)
- Model: CodeLlama 7B by default
- Temperature: 0.1-0.2 for deterministic code generation
- Generates Arduino C++ with proper setup()/loop() structure

**Step 5 AI Tutor:**
- `explainCodeLine()` - Explains individual lines in Korean for beginners
- Interactive code walkthrough with serial monitoring

## Project Data Flow

1. User logs in → JWT token stored in `localStorage`
2. Select kit (Step 1) → `Kit._id` stored in `localStorage` + creates `Project` in DB
3. Brainstorming (Step 2) → Sticky note positions saved to `Project.brainstorming`
4. Hardware Design (Step 3) → Component selection saved to `Project.hardwareDesign`
5. Coding (Step 4) → Q&A generates code → saved to `Project.generatedCode`
6. AI Tutor (Step 5) → Code explanation and serial monitoring
7. Data Upload (Step 6) → Sensor data uploaded to Google Sheets

**State Persistence:**
- `localStorage` keys: `user`, `token`, `selectedKit`, `projectId`, `generatedCode`
- MongoDB `Project` model syncs with backend on each step completion

## Development Workflow

### Adding a New Step
1. Create HTML page in `client/pages/stepX-name.html`
2. Create controller in `client/js/pages/stepX-name.js`
3. Add route in `client/js/router.js`
4. Add backend routes/controllers if API integration needed
5. Update `Project` model schema if new data fields required

### Adding a New Kit
Use seeding script pattern from `server/scripts/seedKits.js`:
```javascript
{
    name: 'Kit Name',
    category: 'Category',
    description: 'Description',
    components: ['Arduino Nano', 'Sensor X'],
    sensors: [
        { name: 'Sensor Name', type: 'FSR402', pin: 'A0', description: 'Purpose' }
    ],
    learningObjectives: ['Objective 1'],
    difficulty: 2,  // 1-3
    duration: 90    // minutes
}
```

### Testing Arduino Code
1. Generate code in Step 4
2. Copy from Step 5 AI Tutor
3. Open Arduino IDE
4. Upload to Arduino board
5. Return to Step 5 → Connect via Web Serial API → Monitor output

## Key Technical Constraints

- **No bundler/build tools** - Pure ES modules loaded via `<script type="module">`
- **No framework** - Vanilla JavaScript with class-based architecture
- **CORS** - Backend must allow `CLIENT_URL` origin
- **MongoDB required** - No fallback to in-memory storage
- **Ollama required** - AI features depend on local Ollama server running
- **Chrome/Edge only** - Web Serial API not available in Firefox/Safari

## Common Issues

### Serial Port Access Denied
- Ensure user gesture triggered the connection (button click)
- Check Arduino is not open in IDE or other serial monitor
- Verify HTTPS or localhost (Web Serial API requires secure context)

### Ollama Connection Failed
- Start Ollama server: `ollama serve`
- Verify model is pulled: `ollama pull codellama:7b`
- Check `OLLAMA_URL` in `.env` matches server address

### JWT Token Expired
- Default expiry is 7 days
- User must log in again
- Check `JWT_EXPIRE` in `.env` to adjust

### MongoDB Connection Error
- Ensure MongoDB is running: `mongod`
- Check `MONGODB_URI` in `.env`
- Database name: `labit-mvp` (from URI path)
