package com.dochkas.jwtAuthScaffold.model.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum Authorities {
    DEFAULT("DEFAULT");

    private final GrantedAuthority authority;

    Authorities(String authority) {
        this.authority = new SimpleGrantedAuthority(authority);
    }

    public static Set<GrantedAuthority> getAllAuthorities() {
        final Set<GrantedAuthority> authorities = new HashSet<>(1);
        for (Authorities a : Authorities.values()) {
            authorities.add(a.getAuthority());
        }
        return authorities;
    }

    public static String getCommaJoinedAuthorities() {
        return Arrays
                .stream(Authorities.values())
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    public static String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
        return collection
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }
}
