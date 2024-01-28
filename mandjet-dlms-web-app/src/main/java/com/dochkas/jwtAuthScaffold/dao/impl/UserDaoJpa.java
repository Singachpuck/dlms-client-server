package com.dochkas.jwtAuthScaffold.dao.impl;

import com.dochkas.jwtAuthScaffold.dao.UserDao;
import com.dochkas.jwtAuthScaffold.model.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface UserDaoJpa extends UserDao, CrudRepository<User, Long> {
}
