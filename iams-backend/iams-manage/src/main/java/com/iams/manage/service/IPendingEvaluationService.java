package com.iams.manage.service;

import com.iams.manage.domain.workflow.dto.PendingEvaluationDTO;

import java.util.List;

public interface IPendingEvaluationService {

    public List<PendingEvaluationDTO> selectPendingEvaluationList();
}
