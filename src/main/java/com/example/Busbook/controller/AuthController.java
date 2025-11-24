package com.example.Busbook.controller;

import com.example.Busbook.entity.User;
import com.example.Busbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> req) {

        String username = req.get("username");
        String password = req.get("password");
        String email = req.get("email");
        String phone = req.get("phone");

        // VALIDATION
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username required");
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password required");
        }
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email required");
        }
        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Phone required");
        }

        // DUPLICATE CHECK
        Optional<User> checkUser = userRepository.findByUsername(username);
        if (checkUser.isPresent()) {
            return ResponseEntity.status(409).body("Username already exists");
        }

        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            return ResponseEntity.status(409).body("Email already exists");
        }

        // SAVE NEW USER
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password.trim());
        user.setEmail(email.trim());
        user.setPhone(phone.trim());

        User saved = userRepository.save(user);

        return ResponseEntity.ok(saved);
    }

    // ✅ LOGIN API - correctly placed inside the class now
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {

        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password required");
        }

        return userRepository.findByUsername(request.getUsername())
                .map(user -> {
                    if (!request.getPassword().equals(user.getPassword())) {
                        return ResponseEntity.status(401).body("Invalid credentials");
                    }
                    return ResponseEntity.ok(Map.of(
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "phone", user.getPhone()
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }
}
