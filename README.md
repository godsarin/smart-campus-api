# Smart Campus Sensor & Room Management API

**Name:** Sarin Pradhan  

**Student ID:** w2083958  

**Module:** 5COSC022W Client-Server Architectures  

A robust, scalable RESTful API built with **JAX-RS (Jersey)** and an embedded **Grizzly HTTP server** for managing campus rooms and IoT sensors — part of the University of Westminster 5COSC022W Client-Server Architectures coursework.

---

## Table of Contents

1. [API Design Overview](#api-design-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [How to Build & Run](#how-to-build--run)
5. [API Endpoints Reference](#api-endpoints-reference)
6. [Sample curl Commands](#sample-curl-commands)
7. [Conceptual Report (Question Answers)](#conceptual-report-question-answers)

---

## API Design Overview

The Smart Campus API follows a **resource-oriented RESTful architecture** with the following design principles:

- **Versioned base path**: All endpoints are prefixed with `/api/v1`
- **Nested sub-resources**: Sensor readings are accessed via `/api/v1/sensors/{sensorId}/readings`
- **Consistent JSON responses**: Every response is wrapped in a uniform `ApiResponse` envelope with `status`, `message`, and `data` fields
- **Meaningful HTTP status codes**: `201 Created`, `404 Not Found`, `409 Conflict`, `422 Unprocessable Entity`, `403 Forbidden`, `500 Internal Server Error`
- **In-memory data store**: `ConcurrentHashMap` and `CopyOnWriteArrayList` for thread-safe, database-free state management
- **Error-proof**: A global `ExceptionMapper<Throwable>` ensures no raw stack traces are ever returned to clients

### Resource Hierarchy

```
/api/v1
├── /                          → Discovery endpoint (HATEOAS metadata)
├── /rooms
│   ├── GET    /               → List all rooms
│   ├── POST   /               → Create a room
│   ├── GET    /{roomId}       → Get room by ID
│   └── DELETE /{roomId}       → Delete room (blocked if sensors exist)
├── /sensors
│   ├── GET    /               → List all sensors (optional ?type= filter)
│   ├── POST   /               → Register a sensor (validates roomId)
│   ├── GET    /{sensorId}     → Get sensor by ID
│   ├── DELETE /{sensorId}     → Remove a sensor
│   └── /{sensorId}/readings   → Sub-resource locator →
│       ├── GET  /             → List all readings for sensor
│       ├── POST /             → Add new reading (updates sensor currentValue)
│       └── GET  /{readingId}  → Get specific reading
```

---

## Technology Stack

| Component         | Technology                          |
|-------------------|-------------------------------------|
| Language          | Java 11                             |
| JAX-RS Impl       | Jersey 2.41                         |
| HTTP Server       | Grizzly2 (embedded, no app server)  |
| JSON Binding      | Jackson via jersey-media-json-jackson |
| Build Tool        | Apache Maven                        |
| Data Storage      | ConcurrentHashMap / CopyOnWriteArrayList (in-memory) |

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── Main.java                          # Entry point, starts Grizzly server
    ├── SmartCampusApplication.java        # JAX-RS @ApplicationPath configuration
    ├── DataStore.java                     # Thread-safe in-memory data store
    ├── JacksonConfig.java                # Custom Jackson ObjectMapper configuration
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   ├── SensorReading.java
    │   ├── ApiResponse.java               # Generic response wrapper
    │   └── ErrorResponse.java             # Error response model
    ├── resource/
    │   ├── DiscoveryResource.java         # GET /api/v1
    │   ├── RoomResource.java              # /api/v1/rooms
    │   ├── SensorResource.java            # /api/v1/sensors
    │   └── SensorReadingResource.java     # /api/v1/sensors/{id}/readings (sub-resource)
    ├── exception/
    │   ├── RoomNotEmptyException.java
    │   ├── RoomNotEmptyExceptionMapper.java        # → HTTP 409
    │   ├── LinkedResourceNotFoundException.java
    │   ├── LinkedResourceNotFoundExceptionMapper.java  # → HTTP 422
    │   ├── SensorUnavailableException.java
    │   ├── SensorUnavailableExceptionMapper.java   # → HTTP 403
    │   └── GlobalExceptionMapper.java              # → HTTP 500 (catch-all)
    └── filter/
        └── LoggingFilter.java             # Request + Response logging filter
```

---

## How to Build & Run

### Prerequisites

- **Java 11+** (check: `java -version`)
- **Apache Maven 3.6+** (check: `mvn -version`)
- Internet connection (first build downloads dependencies)

### Step 1 — Clone the repository

```bash
git clone https://github.com/godsarin/smart-campus-api
cd smart-campus-api
```

### Step 2 — Build the project

```bash
mvn clean package
```

This compiles the code and produces a self-contained executable JAR at:
```
target/smart-campus-api-1.0.0.jar
```

### Step 3 — Start the server

```bash
java -jar target/smart-campus-api-1.0.0.jar
```

You should see:
```
INFO: ====================================================
INFO:   Smart Campus API started successfully!
INFO:   API Root : http://localhost:8080/api/v1/
INFO:   Rooms    : http://localhost:8080/api/v1/rooms
INFO:   Sensors  : http://localhost:8080/api/v1/sensors
INFO:   Press ENTER to stop the server...
INFO: ====================================================
```

### Step 4 — Test the API

Open a new terminal and use `curl` or import the Postman collection.

```bash
curl http://localhost:8080/api/v1
```

### Step 5 — Stop the server

Press **ENTER** in the terminal running the server.

---

## API Endpoints Reference

### Base URL: `http://localhost:8080/api/v1`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Discovery – API metadata & resource links |
| GET | `/rooms` | List all rooms |
| POST | `/rooms` | Create a new room |
| GET | `/rooms/{roomId}` | Get a specific room |
| DELETE | `/rooms/{roomId}` | Delete a room (fails if sensors exist) |
| GET | `/sensors` | List all sensors (supports `?type=` filter) |
| POST | `/sensors` | Register a new sensor |
| GET | `/sensors/{sensorId}` | Get a specific sensor |
| DELETE | `/sensors/{sensorId}` | Remove a sensor |
| GET | `/sensors/{sensorId}/readings` | Get all readings for a sensor |
| POST | `/sensors/{sensorId}/readings` | Record a new reading |
| GET | `/sensors/{sensorId}/readings/{readingId}` | Get a specific reading |

---

## Sample curl Commands

### 1. Discover the API

```bash
curl -X GET http://localhost:8080/api/v1 \
  -H "Accept: application/json"
```

**Response (200 OK):**
```json
{
    "name": "Smart Campus Sensor & Room Management API",
    "version": "1.0.0",
    "description": "RESTful API for managing campus rooms and IoT sensors.",
    "contact": {
        "name": "Campus Facilities Management",
        "email": "facilities@smartcampus.ac.uk",
        "department": "School of Computer Science and Engineering"
    },
    "resources": {
        "rooms": "http://localhost:8080/api/v1/rooms",
        "sensors": "http://localhost:8080/api/v1/sensors"
    },
    "_links": {
        "self": "http://localhost:8080/api/v1",
        "rooms": "http://localhost:8080/api/v1/rooms",
        "sensors": "http://localhost:8080/api/v1/sensors"
    }
}
```

---

### 2. Create a Room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50
  }'
```

**Response (201 Created):**
```json
{
    "status": "success",
    "message": "Room created successfully.",
    "data": {
        "id": "LIB-301",
        "name": "Library Quiet Study",
        "capacity": 50,
        "sensorIds": []
    }
}
```

---

### 3. Register a Sensor linked to the Room

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CO2-001",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 0.0,
    "roomId": "LIB-301"
  }'
```

**Response (201 Created):**
```json
{
    "status": "success",
    "message": "Sensor registered successfully.",
    "data": {
        "id": "CO2-001",
        "type": "CO2",
        "status": "ACTIVE",
        "currentValue": 0.0,
        "roomId": "LIB-301"
    }
}
```

---

### 4. Post a Sensor Reading (updates currentValue)

```bash
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 412.5
  }'
```

**Response (201 Created):**
```json
{
    "status": "success",
    "message": "Reading recorded and sensor 'CO2-001' currentValue updated to 412.5.",
    "data": {
        "id": "babd2841-1e24-4336-9110-ab5ca1045225",
        "timestamp": 1777374668732,
        "value": 412.5
    }
}
```

---

### 5. Retrieve All Sensors Filtered by Type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2" \
  -H "Accept: application/json"
```

**Response (200 OK):**
```json
{
    "status": "success",
    "message": "Retrieved 1 sensor(s).",
    "data": [
        {
            "id": "CO2-001",
            "type": "CO2",
            "status": "ACTIVE",
            "currentValue": 412.5,
            "roomId": "LIB-301"
        }
    ]
}
```

---

### 6. Attempt to Delete a Room with Active Sensors (409 Conflict)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"
```

**Response (409 Conflict):**
```json
{
    "httpStatus": 409,
    "errorCode": "ROOM_NOT_EMPTY",
    "message": "Room 'LIB-301' cannot be deleted because it still has active sensors assigned to it.",
    "timestamp": 1777374779303
}
```

---

### 7. Register a Sensor with Non-Existent Room (422 Unprocessable Entity)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-999",
    "type": "Temperature",
    "status": "ACTIVE",
    "roomId": "GHOST-ROOM"
  }'
```

**Response (422):**
```json
{
    "httpStatus": 422,
    "errorCode": "LINKED_RESOURCE_NOT_FOUND",
    "message": "Referenced Room with id 'GHOST-ROOM' was not found. The request payload is valid JSON, but the referenced resource does not exist.",
    "timestamp": 1777375657954
}
```

---

### 8. Post Reading to a Sensor in MAINTENANCE or OFFLINE (403 Forbidden)

A sensor with status **MAINTENANCE** or **OFFLINE** cannot accept new readings.
Any status other than `ACTIVE` triggers a 403 Forbidden response.

```bash
# Create a room first
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"HALL-1","name":"Main Hall","capacity":200}'

# Create sensor in MAINTENANCE
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-M01","type":"Temperature","status":"MAINTENANCE","roomId":"HALL-1"}'

# Attempt to post a reading → 403
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-M01/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 22.5}'
```

>The same 403 response is returned if the sensor status is `"OFFLINE"` instead of
> `"MAINTENANCE"`, since the API only accepts readings from sensors with status `"ACTIVE"`.


**Response (403 Forbidden):**
```json
{
    "httpStatus": 403,
    "errorCode": "SENSOR_UNAVAILABLE",
    "message": "Sensor 'TEMP-M01' is currently in 'MAINTENANCE' status and cannot accept new readings. Only ACTIVE sensors are able to record data.",
    "timestamp": 1777375892351
}
```

---

## Conceptual Report (Question Answers)

---

### Part 1: Service Architecture & Setup  

**Q:  In your report, explain the default lifecycle of a JAX-RS Resource class. Is a
new instance instantiated for every incoming request, or does the runtime treat it as a
singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures (maps/lists) to prevent data loss or race con
ditions.**

By default, JAX-RS creates a **new instance of each Resource class for every incoming HTTP request** (request-scoped lifecycle). This design improves thread safety at the instance level — each request thread works with its own object, eliminating instance-level race conditions.

However, this means that any instance fields inside a Resource class are **not shared between requests**. If we stored our rooms or sensors in a standard `HashMap` as an instance field, the data would be lost at the end of every request.

To solve this, the Smart Campus API uses a **`DataStore` singleton** with `static` `ConcurrentHashMap` fields. Because `static` fields belong to the class (loaded once by the JVM), they persist across all request instances. `ConcurrentHashMap` provides built-in thread safety, allowing multiple concurrent request threads to read and write safely without explicit `synchronized` blocks. For ordered collections of readings, `CopyOnWriteArrayList` is used, which is optimised for read-heavy workloads.

---

### Part 1.2 — The “Discovery” Endpoint  

**Q: Why is Hypermedia (HATEOAS) considered a hallmark of advanced RESTful design? How does it benefit client developers compared to static documentation?**

HATEOAS (Hypermedia as the Engine of Application State) means embedding navigable links inside API responses so clients can discover available actions dynamically, rather than relying on hard-coded URLs or external documentation.

For example, the discovery endpoint at `GET /api/v1` returns a `_links` map containing the URIs for rooms and sensors. A client that follows these links is not tightly coupled to the URL structure — if the server reorganises its paths, clients that follow links will still work without modification.

This benefits developers because it reduces the "out-of-band knowledge" needed to use the API: the API itself is self-describing, making it easier to explore, harder to break through URL changes, and a better foundation for evolving APIs without forcing simultaneous client upgrades.

---

### Part 2 - Room Management 
### 2.1 - RoomResource Implementation 

**Q: When returning a list of rooms, what are the implications of returning only IDs versus full room objects? Consider network bandwidth and client side
processing.**

Returning **only IDs** reduces payload size — ideal for very large collections — but forces the client to make N additional requests to fetch each room's details, causing the classic "N+1 request problem" which is bandwidth-intensive and slow.

Returning **full objects** costs more bandwidth per response but eliminates extra round-trips, which is nearly always the better trade-off for collections of moderate size. The Smart Campus API returns full objects in the list view, since campus rooms are few enough that the payload remains manageable, and clients benefit from having complete data immediately available for display or processing.

---

### Part 2.2 — RoomDeletion & Safety Logic 

**Q:Is the DELETE operation idempotent in your implementation? Provide a detailed
justification by describing what happens if a client mistakenly sends the exact same DELETE
request for a room multiple times.**

Yes, DELETE is **effectively idempotent** in this implementation in terms of server state. REST's idempotency guarantee means that making the same request N times leaves the server in the same state as making it once.

- **First DELETE** on an existing room (with no sensors): removes the room, returns `200 OK`.
- **Second DELETE** for the same room ID: the room no longer exists, so the server returns `404 Not Found`.

The server **state** is identical after both calls — the room is absent in both cases. The difference in HTTP status code (`200` vs `404`) is acceptable; the REST specification does not require idempotent operations to produce identical response codes, only identical side effects on the resource state. Some implementations return `204 No Content` on both (treating the second as a no-op), which is equally valid. Our approach is transparent — it honestly communicates to the client that the resource was already gone.

---

### Part 3 —  Sensor Operations & Linking 
### Part 3.1 —  Sensor Resource & Integrity  

**Q: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on
the POST method. Explain the technical consequences if a client attempts to send data in
a different format, such as text/plain or application/xml. How does JAX-RS handle this
mismatch?**

The JAX-RS runtime inspects the incoming `Content-Type` request header **before the resource method is invoked**. If the format does not match `application/json`, the framework automatically returns **HTTP 415 Unsupported Media Type** — the resource method body is never executed.

This is a key benefit of declarative annotations: input format validation is handled at the framework level, keeping resource methods clean and free of boilerplate format-checking code. Similarly, if a client sends an `Accept: text/plain` header but the method is annotated `@Produces(APPLICATION_JSON)`, the runtime returns **HTTP 406 Not Acceptable**.

---

### Part 3.2 —  Filtered Retrieval & Search 

**Q:  You implemented this filtering using @QueryParam. Contrast this with an alterna
tive design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why
is the query parameter approach generally considered superior for filtering and searching
collections?**

REST convention distinguishes between **resource identification** (path segments) and **resource refinement** (query parameters):

- **Path segments** (`/sensors/type/CO2`) imply that `CO2` is a distinct child resource of a "type" resource — a hierarchical relationship that does not semantically exist here. This also makes the URL for the base collection (`/sensors`) and the filtered view (`/sensors/type/CO2`) look like entirely different resources.

- **Query parameters** (`/sensors?type=CO2`) express optionality naturally — the base URL `/sensors` cleanly returns all sensors, while `?type=CO2` refines the same collection. Multiple filters compose elegantly (`?type=CO2&status=ACTIVE`). Adding new filter dimensions doesn't alter the URL structure.

Additionally, query parameters are idiomatic for search and filtering in HTTP; intermediaries (caches, proxies) understand this convention, and client libraries handle them consistently.

---
### Part 4: Deep Nesting with Sub- Resources 
### Part 4.1 — Sub-Resource Locator Pattern

**Q:  Discuss the architectural benefits of the Sub-Resource Locator pattern. How
does delegating logic to separate classes help manage complexity in large APIs compared
to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive con
troller class**

The Sub-Resource Locator pattern (used in `SensorResource.getReadingsResource()`) delegates nested path handling to a dedicated class (`SensorReadingResource`) rather than defining all nested routes in one monolithic controller.

**Benefits:**

1. **Single Responsibility**: `SensorResource` handles sensors; `SensorReadingResource` handles readings. Each class is focused and easier to maintain.
2. **Reduced complexity**: A single "God Controller" handling `/sensors`, `/sensors/{id}`, and `/sensors/{id}/readings/{rid}` would become unmanageably large in a real campus API with dozens of resource types.
3. **Independent testability**: `SensorReadingResource` can be unit tested in isolation by constructing it directly with a `sensorId`, without needing a full request pipeline.
4. **Context propagation**: The locator method receives path parameters (`sensorId`) and validates them before instantiating the sub-resource, keeping validation logic co-located with the routing decision.
5. **Scalability**: Adding new sub-resources (e.g., `/sensors/{id}/alerts`) requires only a new locator method and a new class, not modifications to existing code.

---

### Part 5 - Advanced Error Handling, Exception Mapping & Logging 
### Part 5.2 - Dependency Validation (422 Unprocessable Entity) 

**Q: Why is HTTP 422 often considered more semantically accurate than a standard
404 whenthe issue is a missing reference inside a valid JSON payload? **

**HTTP 404 Not Found** means the *requested resource URI* does not exist on the server. It is a routing-level error.

When a client POSTs a valid JSON sensor payload to `/api/v1/sensors` (a URI that exists), but that payload contains a `roomId` that doesn't exist, the endpoint itself was found correctly — the problem is **inside the payload**. The request is syntactically valid (well-formed JSON) but **semantically invalid** (a business-logic constraint is violated).

**HTTP 422 Unprocessable Entity** was designed precisely for this scenario: the server understands the content type and can parse the body, but cannot process the instructions within it due to semantic errors. Returning 422 gives the client a clear signal that the request format was fine but the data was logically incorrect, allowing them to correct the `roomId` value rather than mistakenly assuming they hit the wrong endpoint.

---

### Part 5.4 — The Global Safety Net (500) 

**Q: From a cybersecurity standpoint, explain the risks associated with exposing
internal Java stack traces to external API consumers. What specific information could an
attacker gather from such a trace?**

Exposing raw stack traces to external clients is a significant security vulnerability for several reasons:

1. **Technology fingerprinting**: A stack trace reveals exact library names and versions (e.g., `jersey-server-2.41`, `jackson-databind-2.15.2`). Attackers can cross-reference these against public CVE databases to identify known exploits for those specific versions.

2. **Internal architecture disclosure**: Package names and class names (e.g., `com.smartcampus.DataStore`) reveal the internal code structure, making reverse engineering and targeted attacks significantly easier.

3. **Business logic exposure**: Method names and call chains reveal how the system processes data internally, potentially exposing sensitive logic or data flow paths that can be exploited.

4. **Attack surface mapping**: Line numbers combined with known library code let attackers correlate behaviour with specific code paths, helping them craft inputs that trigger specific failure modes.

The `GlobalExceptionMapper` solves this by logging the full stack trace **server-side** (where it is useful for developers) while returning only a generic, opaque `500 Internal Server Error` message to the client — revealing nothing actionable to a potential attacker.

---

### Part 5.5 — API Request & Response Logging Filters 

**Q: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like
logging, rather than manually inserting Logger.info() statements inside every single re
source method?**

Cross-cutting concerns are behaviours that apply uniformly across all endpoints regardless of business logic. Using JAX-RS filters for logging provides the following advantages over manual insertion:

1. **No duplication (DRY principle)**: One `LoggingFilter` class covers every endpoint automatically. Manual insertion would require dozens of identical log statements across all resource methods.

2. **Consistency**: Filters guarantee uniform log format for every request/response. Manual logging is error-prone — developers may log different fields, use different formats, or forget to add logging to new methods.

3. **Separation of concerns**: Resource methods stay focused on business logic. Logging, authentication, CORS headers, and compression are infrastructure concerns that belong in the filter pipeline, not mixed into domain code.

4. **Maintainability**: If the log format needs to change (e.g., adding a request ID), it changes in one place. With manual logging, every resource method would require modification.

5. **Cannot be forgotten**: New endpoints added to the API are automatically covered by existing filters. There is no risk of accidentally shipping a new endpoint with no observability.
