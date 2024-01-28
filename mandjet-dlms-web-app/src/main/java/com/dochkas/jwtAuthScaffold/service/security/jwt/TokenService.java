package com.dochkas.jwtAuthScaffold.service.security.jwt;

import com.dochkas.jwtAuthScaffold.model.auth.Authorities;
import com.dochkas.jwtAuthScaffold.model.dto.TokenDto;
import com.dochkas.jwtAuthScaffold.service.security.AuthenticationFacade;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${jwt.expiresIn}")
    private int expiresIn;

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    private final AuthenticationFacade authentication;

    public TokenDto generateToken() {
        final TokenDto tokenDto = new TokenDto();
        final Authentication auth = authentication.getAuthentication();
        final String authorities = Authorities.populateAuthorities(auth.getAuthorities());
        final Date issued = new Date();
        tokenDto.setUsername(auth.getName());
        tokenDto.setAuthorities(authorities);
        tokenDto.setExpiresIn(expiresIn);

        // JWT Generation
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        String jwt = Jwts.builder().setIssuer("Dmytro Ochkas").setSubject("JWT Token")
                .claim("username", auth.getName())
                .claim("authorities", authorities)
                .setIssuedAt(issued)
                .setExpiration(new Date(issued.getTime() + expiresIn * 1000L))
                .signWith(key)
                .compact();

        tokenDto.setToken(jwt);

        return tokenDto;
    }
}
