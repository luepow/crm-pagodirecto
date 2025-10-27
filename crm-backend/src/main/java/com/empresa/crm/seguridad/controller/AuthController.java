package com.empresa.crm.seguridad.controller;

import com.empresa.crm.seguridad.dto.AuthResponse;
import com.empresa.crm.seguridad.dto.LoginRequest;
import com.empresa.crm.seguridad.repository.UsuarioRepository;
import com.empresa.crm.shared.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        var usuario = usuarioRepository.findByUsernameWithRoles(userDetails.getUsername())
                .orElseThrow();

        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .roles(roles)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get authenticated user information")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        var usuario = usuarioRepository.findByUsernameWithRoles(authentication.getName())
                .orElseThrow();

        var roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        AuthResponse response = AuthResponse.builder()
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .roles(roles)
                .build();

        return ResponseEntity.ok(response);
    }
}
