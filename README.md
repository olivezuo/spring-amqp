# spring-amqp

This is an example project to show how to use RabbitMQ with Spring Boot.

## RabbitMQ
RabbitMQ is an open source Message Queue system. We are using message system to decouple the system and achieve the scalability and flexiblity.

## Design Highlight

1. Independant message sending service. Easy to intergrate with the actually business service but keep the service code clean.
2. Independant consumer module can be easilly add new consumers. The business service will only focus on the message processing.
3. API to manage the consumer: Stop, Start, Add/remove consumers.
