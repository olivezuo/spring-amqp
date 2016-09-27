# spring-amqp

This is an Spring-Amqp library that can be integrate with any spring boot project that need to use the message queue.

## RabbitMQ
RabbitMQ is an open source Message Queue system. We are using message system to decouple the system and achieve the scalability and flexiblity.

## Design Highlight

1. Independent message sending service. Easy to integrate with the actually business service but keep the service code clean.
2. Independent consumer module can be used to add new consumers. The business service will only focus on the message processing.
3. API to manage the consumer: Stop, Start, Add/remove consumers.
4. Deadletter implementation to retry the message.
5. Send warning email for every failed message, publish or consume.
6. Persist failed message, publish or consume.
7. Retry failed message. 

