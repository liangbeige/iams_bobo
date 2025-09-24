package com.iams.manage.service.iot.impl;

import com.iams.common.exception.ServiceException;
import com.iams.common.utils.StringUtils;
import com.iams.common.utils.bean.BeanValidators;
import com.iams.manage.domain.iot.IotAirConditioner;
import com.iams.manage.mapper.iot.IotAirConditionerMapper;
// import com.ruoyi.system.service.IIotAirConditionerService;
import com.iams.manage.service.iot.IIotAirConditionerService;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import javax.validation.Validator;
// import javax.xml.validation.Validator;
import java.util.List;

/**
 * 空调设备信息，存储空调的静态属性及实时状态Service业务层处理
 *
 * @author liuziqi
 * @date 2025-02-26
 */
@Service
public class IotAirConditionerServiceImpl implements IIotAirConditionerService
{
    @Autowired
    private IotAirConditionerMapper iotAirConditionerMapper;

    @Autowired
    protected Validator validator;

    /**
     * 查询空调设备信息，存储空调的静态属性及实时状态
     *
     * @param id 空调设备信息，存储空调的静态属性及实时状态主键
     * @return 空调设备信息，存储空调的静态属性及实时状态
     */
    @Override
    public IotAirConditioner selectIotAirConditionerById(String id)
    {
        return iotAirConditionerMapper.selectIotAirConditionerById(id);
    }

    /**
     * 查询空调设备信息，存储空调的静态属性及实时状态列表
     *
     * @param iotAirConditioner 空调设备信息，存储空调的静态属性及实时状态
     * @return 空调设备信息，存储空调的静态属性及实时状态
     */
    @Override
    public List<IotAirConditioner> selectIotAirConditionerList(IotAirConditioner iotAirConditioner)
    {
        return iotAirConditionerMapper.selectIotAirConditionerList(iotAirConditioner);
    }

    /**
     * 新增空调设备信息，存储空调的静态属性及实时状态
     *
     * @param iotAirConditioner 空调设备信息，存储空调的静态属性及实时状态
     * @return 结果
     */
    @Override
    public int insertIotAirConditioner(IotAirConditioner iotAirConditioner)
    {
        return iotAirConditionerMapper.insertIotAirConditioner(iotAirConditioner);
    }

    /**
     * 修改空调设备信息，存储空调的静态属性及实时状态
     *
     * @param iotAirConditioner 空调设备信息，存储空调的静态属性及实时状态
     * @return 结果
     */
    @Override
    public int updateIotAirConditioner(IotAirConditioner iotAirConditioner)
    {
        return iotAirConditionerMapper.updateIotAirConditioner(iotAirConditioner);
    }

    /**
     * 批量删除空调设备信息，存储空调的静态属性及实时状态
     *
     * @param ids 需要删除的空调设备信息，存储空调的静态属性及实时状态主键
     * @return 结果
     */
    @Override
    public int deleteIotAirConditionerByIds(String[] ids)
    {
        return iotAirConditionerMapper.deleteIotAirConditionerByIds(ids);
    }

    /**
     * 删除空调设备信息，存储空调的静态属性及实时状态信息
     *
     * @param id 空调设备信息，存储空调的静态属性及实时状态主键
     * @return 结果
     */
    @Override
    public int deleteIotAirConditionerById(String id)
    {
        return iotAirConditionerMapper.deleteIotAirConditionerById(id);
    }

    @Override
    public String getTopicById(String id) {
        return iotAirConditionerMapper.getTopicById(id);
    }

    @Override
    public String importIotAirConditioner(List<IotAirConditioner> iotAirConditionerList, Boolean updateSupport) {
        if (StringUtils.isNull(iotAirConditionerList) || iotAirConditionerList.size() == 0) {
            throw new ServiceException("导入空调设备数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (IotAirConditioner iotAirConditioner : iotAirConditionerList) {
            try {
                // 验证是否存在该设备
                IotAirConditioner existingDevice = iotAirConditionerMapper.selectIotAirConditionerById(iotAirConditioner.getId());
                if (StringUtils.isNull(existingDevice)) {
                    // 校验数据
                    BeanValidators.validateWithException(validator, iotAirConditioner);

//                    IotAirConditioner lastIotAirConditioner = iotAirConditionerMapper.selectLastIotAirConditioner();
//                    String lastId = lastIotAirConditioner != null ? lastIotAirConditioner.getId() : "SN1000";
//                    int lastIdNumber = Integer.parseInt(lastId.substring(3)); // 去掉 "SN1" 前缀，获取数字部分
//                    int newIdNumber = lastIdNumber + 1;
//                    String newId = "SN1" + String.format("%03d", newIdNumber);
//                    iotAirConditioner.setId(newId);
                    // 插入新设备
                    iotAirConditionerMapper.insertIotAirConditioner(iotAirConditioner);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、设备 " + iotAirConditioner.getId() + " 导入成功");
                } else if (updateSupport) {
                    // 校验数据
                    BeanValidators.validateWithException(validator, iotAirConditioner);

                    // 更新设备
                    iotAirConditionerMapper.updateIotAirConditioner(iotAirConditioner);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、设备 " + iotAirConditioner.getId() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、设备 " + iotAirConditioner.getId() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、设备 " + iotAirConditioner.getId() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }
}
