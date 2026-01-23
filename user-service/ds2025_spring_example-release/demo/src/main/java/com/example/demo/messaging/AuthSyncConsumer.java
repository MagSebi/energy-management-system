package com.example.demo.messaging;

import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.entities.Person;
import com.example.demo.repositories.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthSyncConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AuthSyncConsumer.class);

    private final PersonRepository personRepository;

    public AuthSyncConsumer(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.user.sync.auth}")
    @Transactional
    public void handleAuthSync(SyncMessage message) {
        logger.info("Received sync message from auth-service: {}", message);

        try {
            switch (message.getEventType()) {
                case "USER_CREATED":
                    // Create Person with data from auth-service
                    if (message.getEntityId() == null) {
                        logger.error("USER_CREATED event missing entity_id");
                        return;
                    }
                    if (message.getName() == null || message.getAddress() == null || message.getAge() == null) {
                        logger.error("USER_CREATED event missing Person fields (name, address, age)");
                        return;
                    }

                    // Check if Person already exists (avoid duplicates)
                    if (personRepository.existsById(message.getEntityId())) {
                        logger.warn("Person already exists with id: {}, skipping creation", message.getEntityId());
                        return;
                    }

                    Person person = new Person();
                    person.setId(message.getEntityId());
                    person.setName(message.getName());
                    person.setAddress(message.getAddress());
                    person.setAge(message.getAge());
                    personRepository.save(person);
                    logger.info("Created Person for user: {} with name: {}", message.getEntityId(), message.getName());
                    break;

                case "USER_DELETED":
                    // Delete Person when user is deleted
                    if (personRepository.existsById(message.getEntityId())) {
                        personRepository.deleteById(message.getEntityId());
                        logger.info("Deleted Person for user: {}", message.getEntityId());
                    } else {
                        logger.warn("Person not found for deletion: {}", message.getEntityId());
                    }
                    break;

                case "USER_UPDATED":
                    // Update Person if fields are provided
                    if (message.getEntityId() == null) {
                        logger.error("USER_UPDATED event missing entity_id");
                        return;
                    }
                    personRepository.findById(message.getEntityId()).ifPresentOrElse(existingPerson -> {
                        if (message.getName() != null) {
                            existingPerson.setName(message.getName());
                        }
                        if (message.getAddress() != null) {
                            existingPerson.setAddress(message.getAddress());
                        }
                        if (message.getAge() != null) {
                            existingPerson.setAge(message.getAge());
                        }
                        personRepository.save(existingPerson);
                        logger.info("Updated Person for user: {}", message.getEntityId());
                    }, () -> {
                        logger.warn("Person not found for update: {}", message.getEntityId());
                    });
                    break;

                default:
                    logger.warn("Unknown event type from auth-service: {}", message.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error processing auth sync message: {}", message, e);
            throw e;
        }
    }
}
