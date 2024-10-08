# Notification Service

## Overview

The Notification Service is a microservice designed to send notifications via multiple channels such as email, SMS, and Slack. This document outlines the prerequisites, setup instructions, and how to run the application using Docker.

## Prerequisites

Before running the application, ensure you have the following software installed on your machine:

- **Docker**: Make sure you have Docker installed and running. You can download it from the [official Docker website](https://www.docker.com/get-started).

## Installation and Setup Instructions

Follow these steps to set up and run the Notification Service:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/ValeriChakarov/notification-service.git
   cd notification-service

2. **Build the Project: Ensure you have Maven installed. In the terminal, run**:
   ```bash
   mvn clean install
   
3. **Use Docker to Run the Application: In the terminal, navigate to the project directory and run:**
   ```bash
   docker-compose up --build

4. **This command will pull the required images and start the following services**:
   ```bash
   PostgreSQL on port 5433
   Redis on port 6380
   Zookeeper on port 2182
   Kafka on port 9093
   Notification Service on port 8081

5. **Verifying the Setup**

Once the services are running, you can verify the setup by sending a test request to the API endpoint. You can use Postman or curl. Here’s an example using curl:

Run the following command in your terminal:
```bash
   curl -X POST http://localhost:8081/api/notifications/send \
   -H "Content-Type: application/json" \
   -d '{
   "id": "1",
   "message": "Test Notification",
   "recipient": "recipient@example.com",
   "channel": "email"
   }'

