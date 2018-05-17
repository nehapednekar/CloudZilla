package com.csye6225.spring2018.DAO;

import com.csye6225.spring2018.pojo.User;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface UserDao extends CrudRepository<User, Long>

{

    List<User> findByUsernameAndAndPassword(String username, String password);
    List<User> findByUsername(String username);

}
