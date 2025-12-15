package com.ensam.chatbot.repository;

import com.ensam.chatbot.model.Profile;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends MongoRepository<Profile, String> {

    Optional<Profile> findFirstByFullNameIgnoreCase(String fullName);

}
