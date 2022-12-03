package com.ichuvilin.dao;

import com.ichuvilin.entity.Todos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TodoDAO extends JpaRepository<Todos, Long> {
	List<Todos> findByUserId(Long userId);

}
