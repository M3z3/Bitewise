package com.example.foodsafety;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    private final Map<String, String> userDatabase = new HashMap<>();

    public static class UserDto {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDto user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty() || 
            user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username and password cannot be empty.");
        }

        if (userDatabase.containsKey(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists.");
        }

        userDatabase.put(user.getUsername(), user.getPassword());
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserDto user) {
        if (!userDatabase.containsKey(user.getUsername())) {
            return ResponseEntity.status(401).body("User not found.");
        }

        String storedPassword = userDatabase.get(user.getUsername());
        if (!storedPassword.equals(user.getPassword())) {
            return ResponseEntity.status(401).body("Incorrect password.");
        }

        return ResponseEntity.ok("Login successful!");
    }
}