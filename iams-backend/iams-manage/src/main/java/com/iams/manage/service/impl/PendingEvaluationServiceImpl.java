package com.iams.manage.service.impl;

import com.iams.manage.domain.Archive;
import com.iams.manage.domain.PendingEvaluation;
import com.iams.manage.domain.workflow.dto.PendingEvaluationDTO;
import com.iams.manage.mapper.PendingEvaluationMapper;
import com.iams.manage.service.IPendingEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PendingEvaluationServiceImpl implements IPendingEvaluationService {

    @Autowired
    private PendingEvaluationMapper pendingEvaluationMapper;

    @Autowired
    private ArchiveServiceImpl archiveService;

    @Override
    public List<PendingEvaluationDTO> selectPendingEvaluationList() {

        List<PendingEvaluation> list = pendingEvaluationMapper.selectPendingEvaluationList(new PendingEvaluation());

        if (list != null && list.size() > 0) {
            return list.stream().map(item -> {
                PendingEvaluationDTO dto = new PendingEvaluationDTO();
                dto.setArchiveId(item.getArchiveId());
                Archive archive = archiveService.selectArchiveById(item.getArchiveId());
                dto.setArchiveName(archive.getName());
                dto.setArchiveDangHao(archive.getDanghao());
                dto.setGuidangTime(archive.getGuidangTime());
                dto.setSecretLevel(archive.getSecretLevel());
                dto.setSecretPeroid(archive.getSecretPeroid());
                dto.setRetentionPeriod(archive.getRetentionPeriod());
                dto.setEvaluationReason(item.getEvaluationReason());
                dto.setStatus(item.getStatus());

                return dto;
            }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

}
