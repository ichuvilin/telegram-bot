package com.ichuvilin.dao;

import com.ichuvilin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDAO extends JpaRepository<User, Long> {
	User findByTelegramUserId(Long telegramUserId);
}
