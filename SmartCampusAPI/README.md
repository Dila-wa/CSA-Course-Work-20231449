# Smart Campus API

## Overview

This project is a RESTful web service developed for the **5COSC022W Client-Server Architectures coursework**.  
It implements a **Smart Campus Sensor and Room Management API** using **JAX-RS (Jersey)**, **Maven**, and **Apache Tomcat**.

The API manages:
- Rooms
- Sensors assigned to rooms
- Historical sensor readings

The application uses **in-memory data structures only** such as `ConcurrentHashMap` and `CopyOnWriteArrayList`, which follows the coursework requirement of **not using any database**.

## Technology Stack

- Java 11
- Maven
- JAX-RS (Jersey)
- Apache Tomcat 9
- JSON (Jackson)
- In-memory collections

## API Base URL

```text
http://localhost:8080/smart-campus-api/api/v1
```

## API Design

This API follows RESTful principles by exposing resources through clear URLs.

Main resources:
- `/api/v1`  
  Discovery endpoint
- `/api/v1/rooms`  
  Room collection
- `/api/v1/sensors`  
  Sensor collection
- `/api/v1/sensors/{sensorId}/readings`  
  Nested reading resource for a sensor

### Implemented Features

- Discovery endpoint
- Create and retrieve rooms
- Delete rooms with business-rule validation
- Create sensors linked to rooms
- Filter sensors by type using query parameters
- Add and retrieve sensor readings
- Automatic update of sensor `currentValue` after adding a reading
- Exception mapping for custom errors
- Global exception handling
- Request and response logging

## Project Structure

```text
src/main/java/com/smartcampus/api
├── ApiApplication.java
├── exception
├── filter
├── mapper
├── model
├── resource
└── store
```

Important classes:
- `ApiApplication`  
  Configures the JAX-RS application with `/api/v1`
- `DiscoveryResource`  
  Provides API metadata
- `RoomResource`  
  Handles room endpoints
- `SensorResource`  
  Handles sensor endpoints
- `SensorReadingResource`  
  Handles sub-resource endpoints for readings
- `CampusStore`  
  Stores data in memory
- `ApiLoggingFilter`  
  Logs request and response information
- `ExceptionMapper` classes  
  Return consistent JSON error responses

## How To Build And Run

### Prerequisites

Make sure you have:
- JDK 11 or higher
- Maven
- Apache Tomcat 9
- NetBeans or another Java IDE

### Steps

1. Open the project in NetBeans.
2. Add **Apache Tomcat 9** as the server in NetBeans.
3. Set the project server to **Tomcat 9**.
4. Clean and build the project.
5. Run the project.

### Maven Build Command

```bash
mvn clean package
```

This creates:

```text
target/smart-campus-api.war
```

### Deployment

Deploy the generated WAR file to Tomcat or run directly from NetBeans.

## Step-By-Step Postman Testing

Set the following header for all POST requests:

```text
Content-Type: application/json
```

### 1. Discovery Endpoint

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1
```

Expected:
- `200 OK`
- API information in JSON

### 2. Get All Rooms

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms
```

Expected:
- `200 OK`

### 3. Create Room

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
-H "Content-Type: application/json" \
-d "{\"id\":\"ENG-101\",\"name\":\"Engineering Lab\",\"capacity\":40}"
```

Expected:
- `201 Created`

### 4. Get Specific Room

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms/ENG-101
```

Expected:
- `200 OK`

### 5. Create Sensor

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
-H "Content-Type: application/json" \
-d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":400.0,\"roomId\":\"ENG-101\"}"
```

Expected:
- `201 Created`

### 6. Get All Sensors

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors
```

Expected:
- `200 OK`

### 7. Filter Sensors By Type

```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"
```

Expected:
- `200 OK`

### 8. Add Sensor Reading

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001/readings \
-H "Content-Type: application/json" \
-d "{\"value\":410.5}"
```

Expected:
- `201 Created`

### 9. Get Sensor Readings

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001/readings
```

Expected:
- `200 OK`

### 10. Delete Room That Still Has Sensors

```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/ENG-101
```

Expected:
- `409 Conflict`

### 11. Create Sensor With Invalid Room Reference

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
-H "Content-Type: application/json" \
-d "{\"id\":\"TEMP-999\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":20.0,\"roomId\":\"NO-ROOM\"}"
```

Expected:
- `422 Unprocessable Entity`

## Error Handling

The API does not expose raw Java stack traces to clients.  
Instead, it returns structured JSON error responses.

Implemented mappings:
- `409 Conflict`  
  Room still contains sensors
- `422 Unprocessable Entity`  
  Sensor references a room that does not exist
- `403 Forbidden`  
  Sensor in `MAINTENANCE` cannot accept readings
- `500 Internal Server Error`  
  Catch-all global exception handler

## Logging

A JAX-RS filter logs:
- Incoming request method and URI
- Outgoing response status code

This improves observability and avoids putting logging code in every resource method.

## Coursework Questions And Answers

### Part 1: Service Architecture & Setup

#### 1. What is the default lifecycle of a JAX-RS Resource class? Is a new instance created per request or is it treated as a singleton? How does this affect in-memory data structures?

By default, JAX-RS resource classes are **request-scoped**, which means a new resource instance is normally created for each incoming request. This is useful because it avoids sharing request-specific state between clients. However, if in-memory data were stored directly inside a resource instance, that data would be lost after each request. For this reason, shared application data should be stored in a separate shared component such as a singleton store. In this coursework, the shared data is kept in `CampusStore`, which uses thread-safe structures like `ConcurrentHashMap` and `CopyOnWriteArrayList`. This helps prevent data loss and reduces the risk of race conditions when multiple requests happen at the same time.

#### 2. Why is hypermedia considered a hallmark of advanced RESTful design? How does it benefit client developers?

Hypermedia, often referred to as HATEOAS, means that the server includes links or navigation information inside responses so the client can discover what actions are possible next. This is considered an advanced RESTful feature because it reduces the client’s dependency on hardcoded URL knowledge. Instead of relying only on static documentation, client developers can follow links returned by the API. This improves discoverability, flexibility, and maintainability because the server can evolve more safely without forcing clients to hardcode every path.

### Part 2: Room Management

#### 1. When returning a list of rooms, what are the implications of returning only IDs versus full room objects?

Returning only room IDs reduces response size and saves bandwidth, which is useful when the collection is very large. However, it forces clients to make additional requests to get details for each room. Returning full room objects gives more useful information in a single response and reduces extra client calls, but it increases response size. Therefore, the choice depends on balancing network efficiency and client convenience. In this API, returning full objects is better for usability because facilities systems often need room details immediately.

#### 2. Is the DELETE operation idempotent in your implementation?

Yes, DELETE is idempotent in this implementation. If a room exists and is successfully deleted, sending the same DELETE request again does not cause additional changes to server state. After the first successful deletion, the room is already gone, so repeated DELETE requests simply keep the resource absent. This matches the definition of idempotency because repeating the same request does not create a new side effect after the first successful action.

### Part 3: Sensor Operations & Linking

#### 1. What happens if a client sends data in a format other than `application/json` when `@Consumes(MediaType.APPLICATION_JSON)` is used?

When `@Consumes(MediaType.APPLICATION_JSON)` is applied, the resource method only accepts JSON request bodies. If a client sends another media type such as `text/plain` or `application/xml`, JAX-RS will reject the request because no suitable message body reader is available for that content type. In practice, this usually results in an HTTP `415 Unsupported Media Type` response. This protects the API by enforcing a consistent input format.

#### 2. Why is using a query parameter for filtering better than putting the type in the path?

A query parameter such as `/sensors?type=CO2` is better because it clearly expresses that the client is filtering an existing collection rather than requesting a completely different resource. Path parameters are better suited for identifying specific resources or hierarchical paths, while query parameters are more appropriate for optional filters, search criteria, pagination, and sorting. Therefore, `@QueryParam` is the more RESTful and flexible design for collection filtering.

### Part 4: Deep Nesting With Sub-Resources

#### 1. What are the benefits of the Sub-Resource Locator pattern?

The Sub-Resource Locator pattern improves modularity by delegating nested paths to dedicated classes. Instead of placing all sensor and reading logic inside one very large resource class, the nested reading functionality is moved into `SensorReadingResource`. This makes the code easier to understand, maintain, test, and extend. In larger APIs, separating nested responsibilities reduces complexity and supports cleaner design.

#### 2. How is data consistency maintained when a new reading is created?

When a new reading is successfully added to a sensor, the API immediately updates the parent sensor’s `currentValue` field with the latest reading value. This ensures that the historical data and the current sensor state remain consistent. As a result, clients can request the sensor collection and still see the latest measurement without always fetching the full reading history.

### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### 1. Why is HTTP 422 more semantically accurate than 404 for a missing linked resource inside a valid JSON payload?

HTTP `422 Unprocessable Entity` is often more accurate because the request itself is syntactically valid and the target endpoint exists, but the data inside the JSON body contains a semantic problem. In this coursework, posting a sensor with a non-existing `roomId` means the JSON structure is correct, but the relationship is invalid. A `404 Not Found` is usually more appropriate when the requested URL itself does not exist, not when a referenced dependency inside the payload is missing.

#### 2. What are the cybersecurity risks of exposing internal Java stack traces?

Exposing raw Java stack traces can reveal sensitive internal information about the application. An attacker may learn package names, class names, framework versions, server structure, file paths, line numbers, and implementation details. This information can help an attacker identify weaknesses, target known vulnerabilities, or understand how to exploit the system more effectively. For this reason, the API returns generic JSON error responses instead of exposing internal exceptions.

#### 3. Why use JAX-RS filters for logging instead of adding `Logger.info()` in every resource method?

Filters are better for logging because logging is a cross-cutting concern that applies to many endpoints. If logging were added manually inside every resource method, the code would become repetitive and harder to maintain. Using JAX-RS request and response filters centralizes logging in one place, reduces duplication, and ensures consistent logging behavior across the entire API.

## Conclusion

This coursework demonstrates how to build a RESTful API using JAX-RS with nested resources, filtering, business validation, exception mapping, and logging. The final solution follows the coursework rules by using JAX-RS only, avoiding databases, and keeping all data in memory.

## Author

- Student Name: `YOUR NAME HERE`
- Student ID: `YOUR ID HERE`
- Module: `5COSC022W Client-Server Architectures`
