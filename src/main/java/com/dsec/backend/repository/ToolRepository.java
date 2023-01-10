package com.dsec.backend.repository;

import com.dsec.backend.entity.Tool;
import com.dsec.backend.entity.ToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ToolRepository extends JpaRepository<ToolEntity, Integer>, JpaSpecificationExecutor<ToolEntity> {

    ToolEntity findByToolName(Tool tool);

}
