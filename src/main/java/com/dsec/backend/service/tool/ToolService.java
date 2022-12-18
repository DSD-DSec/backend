package com.dsec.backend.service.tool;

import com.dsec.backend.entity.Language;
import com.dsec.backend.entity.RepoDomain;
import com.dsec.backend.entity.RepoType;
import com.dsec.backend.entity.ToolEntity;

import java.util.ArrayList;
import java.util.List;

public interface ToolService {

    List<ToolEntity> getTools();

    ToolEntity getTool(int id);

    ArrayList<Integer> priorityMatrix(RepoType type, RepoDomain domain, Integer security, Integer privacy, Boolean userData, Language language);
}
