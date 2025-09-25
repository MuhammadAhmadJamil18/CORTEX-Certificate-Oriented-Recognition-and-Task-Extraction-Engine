package org.qualitydxb.dal.Repositories;

import org.qualitydxb.dal.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserEmail(String useremail);

    List<User> findByClientIdAndUserRole(Integer clientId, Integer userRole);

    List<User> findByClientId(Integer clientId);

    User findByUserId(Integer userId);

    User findByUserNameIgnoreCase(String userName);
}

