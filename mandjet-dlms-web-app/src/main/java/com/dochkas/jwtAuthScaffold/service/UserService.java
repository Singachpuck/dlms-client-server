package com.dochkas.jwtAuthScaffold.service;

import com.dochkas.jwtAuthScaffold.dao.UserDao;
import com.dochkas.jwtAuthScaffold.exception.EntityException;
import com.dochkas.jwtAuthScaffold.exception.NotOwnerException;
import com.dochkas.jwtAuthScaffold.mapper.UserMapper;
import com.dochkas.jwtAuthScaffold.model.dto.UserDto;
import com.dochkas.jwtAuthScaffold.model.entities.User;
import com.dochkas.jwtAuthScaffold.service.security.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationFacade auth;

    @Value("${entity.user.max-number}")
    private long userMaxCount;

    public UserDto create(UserDto userDto) {
        if (userDao.existsByUsername(userDto.getUsername())) {
            throw new EntityException("Username is already taken.");
        }
        if (userDao.existsByEmail(userDto.getEmail())) {
            throw new EntityException("Email is already taken.");
        }
        if (userDao.count() >= userMaxCount) {
            throw new EntityException("Max number of users exceeded. Contact an administrator for further information.");
        }
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userMapper.userToDto(userDao.save(userMapper.dtoToUser(userDto)));
    }

    public UserDto update(String username, UserDto userDto, BindingResult result) {
        final String currentUsername = this.getCurrentUsername();
        if (!username.equals(currentUsername)) {
            throw new NotOwnerException("User " + currentUsername + " can not perform this operation.");
        }
        final User user = this.findByUsername(username);
        if (userDto.getUsername() != null) {
            if (result.hasFieldErrors("username")) {
                throw new EntityException(this.findErrorMessage("username", result));
            }
            if (userDao.existsByUsername(userDto.getUsername())) {
                throw new EntityException("Username is already taken.");
            }
            user.setUsername(userDto.getUsername());
        }
        if (userDto.getEmail() != null) {
            if (result.hasFieldErrors("email")) {
                throw new EntityException(this.findErrorMessage("email", result));
            }
            if (userDao.existsByEmail(userDto.getEmail())) {
                throw new EntityException("Email is already taken.");
            }
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getPassword() != null) {
            if (result.hasFieldErrors("password")) {
                throw new EntityException(this.findErrorMessage("password", result));
            }
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        return userMapper.userToDto(userDao.save(user));
    }

    private String findErrorMessage(String fieldName, BindingResult result) {
        return Optional.ofNullable(result.getFieldError(fieldName))
                .map(FieldError::getDefaultMessage)
                .orElse("Can't update " + fieldName + ". Value '" + result.getFieldValue(fieldName) + "' is not valid.");
    }

    public void delete(String username) {
        if (!userDao.existsByUsername(username)) {
            throw new EntityException("User with username \"" + username + "\" does not exist.");
        }
        final String currentUsername = this.getCurrentUsername();
        if (!username.equals(currentUsername)) {
            throw new NotOwnerException("User " + currentUsername + " can not perform this operation.");
        }
        userDao.deleteByUsername(username);
    }

    public Collection<User> findAll() {
        return userDao.findAll();
    }

    public User findByUsername(String username) {
        final Optional<User> user = userDao.findByUsername(username);
        if (user.isEmpty()) {
            throw new EntityException("User with username \"" + username + "\" does not exist.");
        }
        return user.get();
    }

    public UserDto findDtoByUsername(String username) {
        return userMapper.userToDto(this.findByUsername(username));
    }

    private String getCurrentUsername() {
        return auth.getAuthentication().getName();
    }
}
