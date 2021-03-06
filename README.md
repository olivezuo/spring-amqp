# spring-amqp

This is an Spring-Amqp library that can be integrate with any spring boot project that need to use the message queue. The library isolate the usage details of a message queue system (Create new Queue, send message, consume message, retry message, manage queue, etc) from the business logic. Using this library the developer can focus only on the business logic and avoid the complexity to handle the queue. 

## RabbitMQ
RabbitMQ is an open source Message Queue system. We are using message system to decouple the system and achieve the scalability and flexibility.

## Design Highlight

1. Independent message sending service. Easy to integrate with the actual business service but keep the service code clean.
2. Independent consumer module can be used to add new consumers. The business service will only focus on the message processing.
3. API to manage the consumer: Stop, Start, Add/remove consumers.
4. DeadLetter implementation to retry the message.
5. Send warning email for every failed message, publish or consume.
6. Persist failed message to MongoDB, publish or consume. (Async process)
7. Retry failed message. We use an API call here to show how it works. Actually it should integrate with a cron job framework to clean up the failed message as a cron job. 

