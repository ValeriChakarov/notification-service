Prerequisites: 
Make sure you have the following software installed on your machine. 
•	Docker

To run the application, follow these steps:
1.	Clone the Repository:
   •	git clone https://github.com/ValeriChakarov/notification-service.git
   •	cd notification-service
2.	Build the Project:
   •	mvn clean install
3. Use Docker to Run the Application:
   •	In the terminal, navigate to the project directory and run:
   •	docker-compose up –build	

This command will pull the required images and start the following services:
   •	PostgreSQL on port 5433
   
   •	Redis on port 6380
   
   •	Zookeeper on port 2182
   
   •	Kafka on port 9093
   
   •	Notification Service on port 8081

Verifying the Setup:
•	You can use Postman or curl to send a test request to the API endpoint:

curl -X POST http://localhost:8081/api/notification -H "Content-Type: application/json" -d '{
"id": "1",
 "message": "Test Notification", 
"recipient": "recipient@example.com", 
"channel": "email"
}'
