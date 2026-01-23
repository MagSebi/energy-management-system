package com.example.demo.services;


import com.example.demo.clients.AuthClient;
import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.dtos.builders.PersonBuilder;
import com.example.demo.entities.Person;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.messaging.SyncPublisher;
import com.example.demo.repositories.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PersonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
    private final PersonRepository personRepository;
    private final AuthClient authClient;
    private final SyncPublisher syncPublisher;

    @Autowired
    public PersonService(PersonRepository personRepository, AuthClient authClient, SyncPublisher syncPublisher) {
        this.personRepository = personRepository;
        this.authClient = authClient;
        this.syncPublisher = syncPublisher;
    }

    public List<PersonDTO> findPersons() {
        List<Person> personList = personRepository.findAll();
        return personList.stream()
                .map(PersonBuilder::toPersonDTO)
                .collect(Collectors.toList());
    }

    public PersonDetailsDTO findPersonById(UUID id) {
        Optional<Person> prosumerOptional = personRepository.findById(id);
        if (prosumerOptional.isEmpty()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        return PersonBuilder.toPersonDetailsDTO(prosumerOptional.get());
    }

    public UUID insert(PersonDetailsDTO personDTO) {
        Person person = PersonBuilder.toEntity(personDTO);
        person = personRepository.save(person);
        LOGGER.debug("Person with id {} was inserted in db", person.getId());

        // Publish sync event
        syncPublisher.publishUserCreated(person.getId(), person.getName());

        return person.getId();
    }

    public PersonDetailsDTO update(UUID id, PersonDetailsDTO dto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id));
        person.setName(dto.getName());
        person.setAddress(dto.getAddress());
        person.setAge(dto.getAge());
        Person updated = personRepository.save(person);

        // Publish sync event
        syncPublisher.publishUserUpdated(updated.getId(), updated.getName());

        return PersonBuilder.toPersonDetailsDTO(updated);
    }

    public void delete(UUID id, String authHeader) {
        if (!personRepository.existsById(id)) {
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        var current = authClient.getCurrentUser(authHeader);
        if (current != null && current.id() != null && current.id().equals(id) && "ADMIN".equalsIgnoreCase(current.role())) {
            throw new IllegalArgumentException("ADMIN self-delete is not allowed");
        }
        authClient.cascadeDeleteUserWithAuth(id, authHeader);
        personRepository.deleteById(id);

        // Publish sync event
        syncPublisher.publishUserDeleted(id);
    }

}
