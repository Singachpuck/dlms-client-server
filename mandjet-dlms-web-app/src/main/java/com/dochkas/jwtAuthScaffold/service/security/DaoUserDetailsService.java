package com.dochkas.jwtAuthScaffold.service.security;

import com.dochkas.jwtAuthScaffold.dao.UserDao;
import com.dochkas.jwtAuthScaffold.mapper.UserMapper;
import com.dochkas.jwtAuthScaffold.model.entities.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class DaoUserDetailsService implements UserDetailsService {

    private UserDao userDao;

    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Optional<User> user = userDao.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User " + username + "does not exist.");
        }
        return userMapper.entityToUserDetails(user.get());
    }
}
