package com.example.foodsafety.controller;

import com.example.foodsafety.model.UserFoodProfile;
import com.example.foodsafety.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserFoodProfile createOrUpdate(@Valid @RequestBody UserFoodProfile profile) {
        return service.save(profile);
    }

    @GetMapping
    public Collection<UserFoodProfile> list() {
        return service.all().values();
    }

    @GetMapping("/{id}")
    public UserFoodProfile get(@PathVariable String id) {
        return service.find(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        if (!service.delete(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
    }
}
