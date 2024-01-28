package com.dochkas.jwtAuthScaffold.dao;


import com.dochkas.jwtAuthScaffold.model.entities.User;

import java.util.Collection;
import java.util.Optional;

public interface UserDao {

    Optional<User> findById(Long id);

    Collection<User> findAll();

    Optional<User> findByUsername(String username);

    User save(User user);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void deleteByUsername(String username);

    long count();
}
