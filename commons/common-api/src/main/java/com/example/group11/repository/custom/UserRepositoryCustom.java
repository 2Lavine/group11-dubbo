package com.example.group11.repository.custom;


import com.example.group11.entity.User;
import com.example.group11.vo.query.UserQueryVO;

import java.util.List;

public interface UserRepositoryCustom {

    List<User> queryFanListByUserId(Long userId, UserQueryVO params);

}

