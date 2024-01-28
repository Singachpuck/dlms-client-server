package com.dochkas.jwtAuthScaffold.controller;

import com.dochkas.jwtAuthScaffold.model.dto.UserDto;
import com.dochkas.jwtAuthScaffold.service.UserService;
import com.dochkas.jwtAuthScaffold.service.security.jwt.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthRestController {

    private final UserService userService;

    private final TokenService tokenService;

    @PostMapping("/auth/token")
    ResponseEntity<?> token() {
        return ResponseEntity.ok(tokenService.generateToken());
    }

    @GetMapping("/users")
    ResponseEntity<?> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping("/signup")
    ResponseEntity<?> register(@Valid @RequestBody UserDto user) {
        final UserDto created = userService.create(user);

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{username}")
                .buildAndExpand(created.getUsername())
                .toUri();

        return ResponseEntity
                .created(uri)
                .build();
    }

    @GetMapping("/users/{username}")
    ResponseEntity<?> get(@PathVariable String username) {
        return ResponseEntity.ok(userService.findDtoByUsername(username));
    }

    @PatchMapping("/users/{username}")
    ResponseEntity<?> update(@PathVariable String username, @Valid @RequestBody UserDto user, BindingResult result) {
        return ResponseEntity.ok(userService.update(username, user, result));
    }

    @DeleteMapping("/users/{username}")
    ResponseEntity<?> delete(@PathVariable String username) {
        userService.delete(username);
        return ResponseEntity.noContent().build();
    }
}
