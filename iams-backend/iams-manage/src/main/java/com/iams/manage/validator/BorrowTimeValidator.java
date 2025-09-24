package com.iams.manage.validator;

import com.iams.common.utils.DateUtils;
import com.iams.manage.domain.workflow.dto.BorrowRecordDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Calendar;
import java.util.Date;


/*
针对借阅时间进行校验，前端新增的借阅开始时间不得晚于今天，结束时间不得小于借阅开始时间
 */
public class BorrowTimeValidator implements ConstraintValidator<ValidBorrowTime, BorrowRecordDTO> {

    @Override
    public boolean isValid(BorrowRecordDTO value, ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();

        Date today = DateUtils.getNowDate();
        Date startDate = value.getStartDate();
        Date endDate = value.getEndDate();

        today = DateUtils.truncate(today, Calendar.DATE);
        startDate = DateUtils.truncate(startDate, Calendar.DATE);

        // 允许今天：当且仅当 startDate < today 时报错
        if (startDate.before(today)) {

            context.buildConstraintViolationWithTemplate("借阅日期不能早于当前日期").addConstraintViolation();
            return false;
        }

        // 结束日期必须 >= 开始日期
        if (endDate.before(startDate)) {
            context.buildConstraintViolationWithTemplate("归还日期不能早于借阅日期").addConstraintViolation();
            return false;
        }

        return true;
    }
}
