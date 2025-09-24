package com.iams.manage.validator;

import com.iams.manage.domain.Archive;
import com.iams.manage.domain.workflow.dto.CommonRecordBaseDTO;
import com.iams.manage.service.IArchiveService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ArchiveInfoValidator implements ConstraintValidator<ValidArchiveInfo, CommonRecordBaseDTO> {

    @Autowired
    private IArchiveService archiveService;

    public boolean isValid(CommonRecordBaseDTO value, ConstraintValidatorContext context) {

        if(value == null)
        {   // 空数据是可以的，比如getList函数
            return true;
        }
        context.disableDefaultConstraintViolation();

        Archive archive = archiveService.selectArchiveById(value.getArchiveId());
        if(archive == null)
        {
            context.buildConstraintViolationWithTemplate("档案ID不存在").addConstraintViolation();
            // 检查档案是否存在
            return false;
        }
        if(archive.getStatus().equals("Destroyed"))
        {
            context.buildConstraintViolationWithTemplate("档案已被销毁").addConstraintViolation();
            return false;
        }
        if(archive.getName().equals(value.getArchiveName())
                && archive.getDanghao().equals(value.getArchiveDangHao()))
        {
            return true;
        }
        context.buildConstraintViolationWithTemplate("档案ID、档号、档案名不一致").addConstraintViolation();
        return false;
    }
}
