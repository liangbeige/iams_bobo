package com.iams.manage.service.iot.impl;

import com.iams.common.exception.ServiceException;
import com.iams.common.utils.StringUtils;
import com.iams.common.utils.bean.BeanValidators;
import com.iams.manage.domain.iot.IotExhaustFun;
import com.iams.manage.mapper.iot.IotExhaustFunMapper;
import com.iams.manage.service.iot.IIotExhaustFunService;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import javax.validation.Validator;
import java.util.List;

/**
 * 排风扇设备信息Service业务层处理
 *
 * @author liuziqi
 * @date 2025-02-28
 */
@Service
public class IotExhaustFunServiceImpl implements IIotExhaustFunService
{
    @Autowired
    private IotExhaustFunMapper iotExhaustFunMapper;

    @Autowired
    protected Validator validator;

    /**
     * 查询排风扇设备信息
     *
     * @param deviceId 排风扇设备信息主键
     * @return 排风扇设备信息
     */
    @Override
    public IotExhaustFun selectIotExhaustFunByDeviceId(String deviceId)
    {
        return iotExhaustFunMapper.selectIotExhaustFunByDeviceId(deviceId);
    }

    /**
     * 查询排风扇设备信息列表
     *
     * @param iotExhaustFun 排风扇设备信息
     * @return 排风扇设备信息
     */
    @Override
    public List<IotExhaustFun> selectIotExhaustFunList(IotExhaustFun iotExhaustFun)
    {
        return iotExhaustFunMapper.selectIotExhaustFunList(iotExhaustFun);
    }

    /**
     * 新增排风扇设备信息
     *
     * @param iotExhaustFun 排风扇设备信息
     * @return 结果
     */
    @Override
    public int insertIotExhaustFun(IotExhaustFun iotExhaustFun)
    {
        return iotExhaustFunMapper.insertIotExhaustFun(iotExhaustFun);
    }

    /**
     * 修改排风扇设备信息
     *
     * @param iotExhaustFun 排风扇设备信息
     * @return 结果
     */
    @Override
    public int updateIotExhaustFun(IotExhaustFun iotExhaustFun)
    {
        return iotExhaustFunMapper.updateIotExhaustFun(iotExhaustFun);
    }

    /**
     * 批量删除排风扇设备信息
     *
     * @param deviceIds 需要删除的排风扇设备信息主键
     * @return 结果
     */
    @Override
    public int deleteIotExhaustFunByDeviceIds(String[] deviceIds)
    {
        return iotExhaustFunMapper.deleteIotExhaustFunByDeviceIds(deviceIds);
    }

    /**
     * 删除排风扇设备信息信息
     *
     * @param deviceId 排风扇设备信息主键
     * @return 结果
     */
    @Override
    public int deleteIotExhaustFunByDeviceId(String deviceId)
    {
        return iotExhaustFunMapper.deleteIotExhaustFunByDeviceId(deviceId);
    }

    @Override
    public String getTopicById(String id) {
        return iotExhaustFunMapper.getTopicById(id);
    }

    @Override
    public String importExhaustFun(List<IotExhaustFun> ExhaustFunList, Boolean updateSupport) {
        if (StringUtils.isNull(ExhaustFunList) || ExhaustFunList.size() == 0) {
            throw new ServiceException("导入空调设备数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (IotExhaustFun iotExhaustFun : ExhaustFunList) {
            try {
                // 验证是否存在该设备
                IotExhaustFun existingDevice = iotExhaustFunMapper.selectIotExhaustFunByDeviceId(iotExhaustFun.getDeviceId());
                if (StringUtils.isNull(existingDevice)) {
                    // 校验数据
                    BeanValidators.validateWithException(validator, iotExhaustFun);

//                    IotExhaustFun lastIotExhaustFun = iotExhaustFunMapper.selectLastIotExhaustFun();
//                    String lastId = lastIotExhaustFun != null ? lastIotExhaustFun.getDeviceId() : "EXF000";
//                    int lastIdNumber = Integer.parseInt(lastId.substring(3));
//                    int newIdNumber = lastIdNumber + 1;
//                    String newId = "EXF" + String.format("%03d", newIdNumber);
//                    iotExhaustFun.setDeviceId(newId);
                    // 插入新设备
                    iotExhaustFunMapper.insertIotExhaustFun(iotExhaustFun);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、设备 " + iotExhaustFun.getDeviceId() + " 导入成功");
                } else if (updateSupport) {
                    // 校验数据
                    BeanValidators.validateWithException(validator, iotExhaustFun);

                    // 更新设备
                    iotExhaustFunMapper.updateIotExhaustFun(iotExhaustFun);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、设备 " + iotExhaustFun.getDeviceId() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、设备 " + iotExhaustFun.getDeviceId() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、设备 " + iotExhaustFun.getDeviceId() + " 导入失败：";
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
