package com.dochkas.mandjetDlmsWeb.dao.impl;

import com.dochkas.mandjetDlmsWeb.dao.UserDao;
import com.dochkas.mandjetDlmsWeb.model.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface UserDaoJpa extends UserDao, CrudRepository<User, Long> {
}
