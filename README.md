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
