package com.dsec.backend.repository;

import com.dsec.backend.entity.ToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToolRepository  extends JpaRepository<ToolEntity, Long> {
}
