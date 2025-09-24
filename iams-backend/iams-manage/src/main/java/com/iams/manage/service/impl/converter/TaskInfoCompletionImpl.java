package com.iams.manage.service.impl.converter;

import com.iams.manage.domain.TaskDTO.ArchiveTaskDTO;
import com.iams.manage.service.ITaskInfoCompletion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TaskInfoCompletionImpl implements ITaskInfoCompletion {


    @Autowired
    private BorrowRecordConverterImpl borrowRecordConverterImpl;

    @Autowired
    private DestroyRecordConverterImpl destroyRecordConverterImpl;

    @Autowired
    private FilingRecordConverterImpl filingRecordConverterImpl;

    @Autowired
    private IdentificationRecordConverterImpl identificationRecordConverterImpl;

    @Override
    public ArchiveTaskDTO taskInfoCompletion(ArchiveTaskDTO archiveTaskDTO) {


        if(archiveTaskDTO.getInstanceName().contains("Borrow"))
        {
            archiveTaskDTO.setInstanceName("借阅申请");
            return borrowRecordConverterImpl.taskInfoCompletion(archiveTaskDTO);
        }

        else if(archiveTaskDTO.getInstanceName().contains("Destroy"))
        {
            archiveTaskDTO.setInstanceName("销毁申请");
            return destroyRecordConverterImpl.taskInfoCompletion(archiveTaskDTO);
        }

        else if(archiveTaskDTO.getInstanceName().contains("Filing"))
        {
            archiveTaskDTO.setInstanceName("归档申请");
            return filingRecordConverterImpl.taskInfoCompletion(archiveTaskDTO);
        }
        else if(archiveTaskDTO.getInstanceName().contains("Identification"))
        {
            archiveTaskDTO.setInstanceName("鉴定申请");
            return identificationRecordConverterImpl.taskInfoCompletion(archiveTaskDTO);
        }

        return archiveTaskDTO;
    }

}
