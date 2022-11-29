package com.ichuvilin.dao;

import com.ichuvilin.entity.Todos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoDAO extends JpaRepository<Todos, Long> {
	List<Todos> findByUserId(Long userId);
}
