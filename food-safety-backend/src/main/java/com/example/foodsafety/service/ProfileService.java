package com.example.foodsafety.service;

import com.example.foodsafety.model.UserFoodProfile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProfileService {
    private final Map<String, UserFoodProfile> profiles = new ConcurrentHashMap<>();

    public UserFoodProfile save(UserFoodProfile profile) {
        profiles.put(profile.id(), profile);
        return profile;
    }

    public Optional<UserFoodProfile> find(String id) {
        return Optional.ofNullable(profiles.get(id));
    }

    public Map<String, UserFoodProfile> all() {
        return Map.copyOf(profiles);
    }

    public boolean delete(String id) {
        return profiles.remove(id) != null;
    }
}
