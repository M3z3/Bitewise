package com.example.foodsafety;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final ConcurrentMap<String, String> userDatabase = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Username and password cannot be empty.");
        }

        if (user.getPassword().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            return ResponseEntity.badRequest().body("Password must be 72 bytes or fewer.");
        }

        String username = normalizeUsername(user.getUsername());
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        if (userDatabase.putIfAbsent(username, encodedPassword) != null) {
            return ResponseEntity.badRequest().body("Username already exists.");
        }

        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserDto user) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Username and password cannot be empty.");
        }

        String storedPassword = userDatabase.get(normalizeUsername(user.getUsername()));
        if (storedPassword == null || !passwordEncoder.matches(user.getPassword(), storedPassword)) {
            return ResponseEntity.status(401).body("Invalid username or password.");
        }

        return ResponseEntity.ok("Login successful!");
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}