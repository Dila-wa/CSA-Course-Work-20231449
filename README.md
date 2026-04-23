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

Resource instances in JAX-RS are by default **request-scoped**, meaning that a new instance is created for every request received. This is convenient as it prevents client request state from being shared with other clients. However, if any in-memory information were to be maintained in a resource object itself, this information would be lost after each request cycle. Consequently, any application-wide information should be maintained separately within a shared entity such as the singleton store. In this coursework, the shared information is stored in the class `CampusStore`, which employs thread-safe collections such as `ConcurrentHashMap` and `CopyOnWriteArrayList`.

#### 2. Why is hypermedia considered a hallmark of advanced RESTful design? How does it benefit client developers?

Hypermedia, also called HATEOAS, implies that the server provides navigational details in its response so that the client can know about the possible future actions. This is seen as a sophisticated technique in RESTful architecture as it decreases the dependence of the client on fixed URLs. Rather than depending entirely on fixed documentation, the developer can use the links provided in the API response to get the details.

### Part 2: Room Management

#### 1. When returning a list of rooms, what are the implications of returning only IDs versus full room objects?

Returning just the IDs of the rooms keeps the response small and helps save bandwidth, particularly if the collection of resources is extremely large. But in doing so, it compels the client applications to make further requests to obtain all the details of the rooms. Returning complete objects for the rooms provides a more valuable response and helps avoid making other requests from the client application, though the response will be larger. In this case, the second option should be preferred for user-friendliness reasons.

#### 2. Is the DELETE operation idempotent in your implementation?

Indeed, the DELETE method is idempotent in this case. If there is a room that can be successfully deleted by issuing the DELETE command, then issuing it for the second time would not result in any change in the server’s state. The room would have been already deleted the first time, so the subsequent issuance of the DELETE command would only maintain its absence from the system.

### Part 3: Sensor Operations & Linking

#### 1. What happens if a client sends data in a format other than `application/json` when `@Consumes(MediaType.APPLICATION_JSON)` is used?

The usage of whena `@Consumes(MediaType.APPLICATION_JSON)` means that the body of the HTTP request must be in JSON format. If some other media type is specified as the content of the request (e.g., `text/plain` or `application/xml`), the request will not be recognized by JAX-RS due to the lack of the appropriate message body reader for that content. As a consequence, HTTP `(415 Unsupported Media Type)` will occur.

#### 2. Why is using a query parameter for filtering better than putting the type in the path?

The query parameter `/sensors?type=CO2` is preferable because it conveys explicitly that the client wants to filter the collection rather than request something else entirely. Query parameters work best when you want to do additional things like filtering/searching/sorting/page, while path parameters should be used for uniquely identifying resources or hierarchical paths within your domain. Hence, `@QueryParam` is the way to go here.

### Part 4: Deep Nesting With Sub-Resources

#### 1. What are the benefits of the Sub-Resource Locator pattern?

The Sub-Resource Locator pattern achieves modularity through delegation of nested paths to separate classes. Rather than having all the sensors and readings code in one gigantic resource class, the nested path for the readings is put in `SensorReadingResource`. This allows for better understanding, maintenance, testing, and extensibility of the code.

#### 2. How is data consistency maintained when a new reading is created?

Once a new reading has been added to the sensor successfully, the API updates the `currentValue` property of the parent sensor object with the current reading. This makes sure that the data is always consistent between the past records and the current state of the sensor. This way, when a client requests for all sensors, they can get the current reading without requesting the whole history.

### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### 1. Why is HTTP 422 more semantically accurate than 404 for a missing linked resource inside a valid JSON payload?

HTTP `422 Unprocessable Entity` makes more sense than `404 Not Found` since the request is a valid one and the resource endpoint being referred to by the request does exist, although there is an issue with the data in the JSON payload. For instance, in the current coursework, if we post a sensor with a non-existent `roomId`, the structure of the JSON request is correct, yet the association is faulty.

#### 2. What are the cybersecurity risks of exposing internal Java stack traces?

Unobscured stack trace from Java could contain confidential internal details about the application. Attackers can gather information about packages, classes, version of frameworks used, server architecture, file paths, line numbers, and even code. This could allow them to discover weak points and exploits in the application. In order to avoid leaking such critical data, the API response contains JSON error messages only.

#### 3. Why use JAX-RS filters for logging instead of adding `Logger.info()` in every resource method?

The use of filters is more efficient than manual logging since logging is a crosscutting concern, meaning that it can be applied to multiple resources. The implementation of logging manually by adding logging statements in each resource would make the code repetitive and difficult to manage. Using filters in JAX-RS for requests and responses allows for centralized logging.

## Conclusion

This coursework demonstrates how to build a RESTful API using JAX-RS with nested resources, filtering, business validation, exception mapping, and logging. The final solution follows the coursework rules by using JAX-RS only, avoiding databases, and keeping all data in memory.

## Author

- Student Name: `Senuth Dilnada Wanniarachchi`
- Student ID: `20231449 / w2120331`
- Module: `5COSC022W Client-Server Architectures`
