package com.iams.manage.service.impl;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iams.common.core.domain.entity.SysUser;
import com.iams.system.service.ISysUserService;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.nio.charset.Charset;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import java.util.Arrays;
import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;
import com.iams.common.utils.DateUtils;
import com.iams.common.utils.SecurityUtils;
import com.iams.common.utils.uuid.UUID;
import com.iams.manage.domain.Archive;
import com.iams.manage.domain.workflow.dto.BorrowRecordDTO;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.service.IMailService;
import com.iams.manage.service.IReminderMessageService;
import com.iams.manage.service.impl.converter.BorrowRecordConverterImpl;
import com.iams.system.domain.SysPost;
import com.iams.system.domain.SysUserPost;
import com.iams.system.mapper.SysPostMapper;
import com.iams.system.mapper.SysUserMapper;
import com.iams.system.mapper.SysUserPostMapper;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders; // 使用Spring的HttpHeaders
import com.iams.manage.mapper.BorrowRecordMapper;
import com.iams.manage.domain.workflow.BorrowRecord;
import com.iams.manage.service.IBorrowRecordService;
import com.iams.activiti8.service.impl.WorkFlowStarterImpl;

import com.iams.manage.domain.UserGroups;
/**
 * 借阅记录Service业务层处理
 * 
 * @author LiuTao
 * @date 2025-04-07
 */
@Service
public class BorrowRecordServiceImpl implements IBorrowRecordService
{
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private BorrowRecordConverterImpl borrowRecordConverterImpl;

    @Autowired
    private WorkFlowStarterImpl workFlowStarter;


    @Autowired
    private ArchiveMapper archiveMapper;


    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ISysUserService userService;


    @Autowired
    private IMailService mailService;

    @Autowired
    private TaskListImpl taskList;

    @Autowired
    private SysUserPostMapper sysUserPostMapper;

    @Autowired
    private SysPostMapper sysPostMapper;

    @Autowired
    private IReminderMessageService reminderMessageService;


    /**
     * 查询借阅记录
     * 
     * @param id 借阅记录主键
     * @return 借阅记录
     */
    @Override
    public BorrowRecordDTO selectBorrowRecordById(Long id)
    {
        // 判断当前用户是否为（审核员、馆长）
        // 一般情况下自己查自己，其他情况下必须是管理员
        if(SecurityUtils.hasGroupPermission(SecurityUtils.getLoginUser(), UserGroups.ALL_GROUPS))
        {
            return borrowRecordConverterImpl.entityToDto(borrowRecordMapper.selectBorrowRecordById(id));
        }
        else
        {
            return borrowRecordConverterImpl.entityToDto(borrowRecordMapper.selectBorrowRecordById(SecurityUtils.getLoginUser().getUserId()));
        }
    }

    /**
     * 查询借阅记录列表
     * 
     * @param borrowRecord 借阅记录
     * @return 借阅记录
     */
    @Override
    public List<BorrowRecordDTO> selectBorrowRecordList(BorrowRecord borrowRecord)
    {
        // 权限判断逻辑保持不变
        if(SecurityUtils.hasGroupPermission(SecurityUtils.getLoginUser(), UserGroups.ALL_GROUPS) && borrowRecord.getUserId()==null)
        {
            borrowRecord.setUserId(null);
        }
        else if (borrowRecord.getUserId()==null)
        {
            borrowRecord.setUserId(SecurityUtils.getLoginUser().getUserId());
        }

        // 获取分页查询结果（这里会是Page类型）
        List<BorrowRecord> borrowRecordList = borrowRecordMapper.selectBorrowRecordList(borrowRecord);

        // 转换数据但保持分页信息
        List<BorrowRecordDTO> dtoList = borrowRecordList.stream()
                .map(borrowRecordEntity -> borrowRecordConverterImpl.entityToDto(borrowRecordEntity))
                .collect(Collectors.toList());

        // 如果原列表是Page类型，需要保持分页信息
        if (borrowRecordList instanceof com.github.pagehelper.Page) {
            com.github.pagehelper.Page<BorrowRecord> page = (com.github.pagehelper.Page<BorrowRecord>) borrowRecordList;
            com.github.pagehelper.Page<BorrowRecordDTO> dtoPage = new com.github.pagehelper.Page<>();
            dtoPage.addAll(dtoList);
            dtoPage.setTotal(page.getTotal());
            dtoPage.setPageNum(page.getPageNum());
            dtoPage.setPageSize(page.getPageSize());
            dtoPage.setPages(page.getPages());
            return dtoPage;
        }

        return dtoList;
    }


    /**
     * 查询已逾期的实体档案借阅记录列表，需要设定状态：已逾期
     *
     * @param borrowRecord 借阅记录
     * @return 借阅记录
     */
    @Override
    public List<BorrowRecordDTO> selectOverdueBorrowRecordList(BorrowRecord borrowRecord)
    {
        borrowRecord.setStatus("已逾期");
        return selectBorrowRecordList(borrowRecord);
    }

    /**
     * 新增借阅记录
     * 
     * @param borrowRecordDTO 借阅记录
     * @return 结果
     */
    @Override
    public int insertBorrowRecord(BorrowRecordDTO borrowRecordDTO)
    {
        // 以下代码做成验证器似乎有些麻烦，要些四个验证器，不如在这里做了。
        // 档案验证注解：是否存在、状态不是销毁，那就可以借阅
        // 记录表中不能重复借阅：用户ID-档案ID-借阅状态（已批准）
        List<BorrowRecord> borrowRecordList = borrowRecordMapper.selectBorrowRecordList(new BorrowRecord(borrowRecordDTO.getUserId(), borrowRecordDTO.getArchiveId()));
        for(BorrowRecord borrowRecord : borrowRecordList)
        {
            if(borrowRecord.getStatus().equals("已批准"))
            {
                throw new RuntimeException("该档案已被借阅，请勿重复借阅");
            }
            else if(borrowRecord.getStatus().contains("审批"))
            {
                throw new RuntimeException("请勿重复发起申请");
            }
        }
        // 传入DTO，转换成实体
        BorrowRecord borrowRecord = borrowRecordConverterImpl.dtoToEntity(borrowRecordDTO);
        borrowRecord.setBusinessKey(UUID.randomUUID().toString());


        String instanceId = workFlowStarter.startProcessInstanceByDefinitionKey(borrowRecord.getProcessName(), borrowRecord.getBusinessKey(), borrowRecordDTO.getUserName()+"借阅申请");

        borrowRecord.setInstanceId(instanceId);
        borrowRecord.setStartApplyTime(DateUtils.getNowDate());
        borrowRecord.setStatus("待审批");
        return borrowRecordMapper.insertBorrowRecord(borrowRecord);
    }

    /**
     * 修改借阅记录
     * 
     * @param borrowRecordDTO 借阅记录
     * @return 结果
     */
    @Override
    public int updateBorrowRecord(BorrowRecordDTO borrowRecordDTO)
    {
        BorrowRecord borrowRecord = borrowRecordConverterImpl.dtoToEntity(borrowRecordDTO);
        if(borrowRecordMapper.selectBorrowRecordById(borrowRecord.getId()).getStatus().equals("已批准")
        || borrowRecordMapper.selectBorrowRecordById(borrowRecord.getId()).getStatus().equals("已驳回")
        || borrowRecordMapper.selectBorrowRecordById(borrowRecord.getId()).getStatus().equals("已结束"))
        {
            throw new RuntimeException("流程已结束，无法修改");
        }
        return borrowRecordMapper.updateBorrowRecord(borrowRecord);
    }


    @Override
    public int updateOverdueBorrowRecord(BorrowRecordDTO borrowRecordDTO){
        BorrowRecord borrowRecord = borrowRecordMapper.selectBorrowRecordById(borrowRecordDTO.getId());
        borrowRecord.setReturnDate(borrowRecordDTO.getReturnDate());
        borrowRecord.setStatus(borrowRecordDTO.getStatus());
        borrowRecord.setOverdueDays(borrowRecordDTO.getOverdueDays());
        return borrowRecordMapper.updateBorrowRecord(borrowRecord);
    }

    @Override
    public void updateBorrowRecord(BorrowRecord borrowRecord)
    {
        borrowRecordMapper.updateBorrowRecord(borrowRecord);
    }

    /**
     * 批量删除借阅记录
     * 
     * @param ids 需要删除的借阅记录主键
     * @return 结果
     */
    @Override
    public int deleteBorrowRecordByIds(Long[] ids)
    {
        for (Long id : ids)
            deleteBorrowRecordById(id);

        return 1;
    }

    /**
     * 删除借阅记录信息
     * 
     * @param id 借阅记录主键
     * @return 结果
     */
    @Override
    public int deleteBorrowRecordById(Long id)
    {
        workFlowStarter.deleteProcessInstance(borrowRecordMapper.selectBorrowRecordById(id).getInstanceId());
        return borrowRecordMapper.deleteBorrowRecordById(id);
    }

    @Override
    public BorrowRecord selectBorrowRecordByBusinessKey(String businessKey)
    {
        return borrowRecordMapper.selectBorrowRecordByBusinessKey(businessKey);
    }

    /**
     * 定时任务：更新借阅记录状态。
     * 每天凌晨执行，检查所有已批准的借阅记录的结束日期。
     * 如果结束日期在过去，则根据载体类型更新记录状态为"已逾期"或"已结束"，
     * 并计算逾期天数。
     */
    @Override
    @Scheduled(cron = "0 0 * * * ?")  // 每小时的 0 分 0 秒执行（即整点执行）
    public void borrowRecordStatus() {
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setStatus("已批准");

        // 查询所有状态为"已批准"的借阅记录
        borrowRecordMapper.selectBorrowRecordList(borrowRecord).forEach(record -> {
            Date endDate = record.getEndDate();

            if (endDate != null) {
                // 创建日历对象用于日期计算
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);

                // 计算结束日期的后一天（结束日期+1天）
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Date nextDayAfterEnd = calendar.getTime();

                // 检查是否已经过了结束日期的后一天
                if (nextDayAfterEnd.getTime() <= System.currentTimeMillis()) {
                    Archive archive = archiveMapper.selectArchiveById(record.getArchiveId());
                    if (archive != null) {
                        String carrierType = archive.getCarrierType();

                        // 计算逾期天数（从结束日期的后一天开始计算）
                        long diffInMillis = System.currentTimeMillis() - nextDayAfterEnd.getTime();
                        long overdueDays = diffInMillis / (1000 * 60 * 60 * 24); // 转换为天数

                        // 逾期天数至少为1天
                        if (overdueDays < 1) {
                            overdueDays = 1;
                        }

                        // 根据载体类型设置记录状态
                        // 实体档案
                        if ("tangible".equals(carrierType)) {
                            record.setStatus("已逾期");
                            record.setOverdueDays(overdueDays);
                        }
                        // 混合档案
                        else if ("Mixture".equals(carrierType)) {
                            // 判断申请备注中是否包含"电子借阅"
                            if (record.getRemark() != null && record.getRemark().contains("电子借阅")) {
                                record.setStatus("已结束");
                            } else {
                                record.setStatus("已逾期");
                                record.setOverdueDays(overdueDays);
                            }
                        }
                        // 电子档案
                        else {
                            record.setStatus("已结束");
                        }

                        // 更新借阅记录
                        borrowRecordMapper.updateBorrowRecord(record);
                    }
                }
            }
        });
    }

    // 在BorrowRecordServiceImpl类中添加这个方法
    private RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 配置消息转换器
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        // 1. 添加UTF-8编码的字符串转换器
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false); // 防止添加不必要的charset参数
        converters.add(stringConverter);

        // 2. 添加JSON转换器
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();

        // 3. 添加二进制转换器处理octet-stream
        ByteArrayHttpMessageConverter byteArrayConverter = new ByteArrayHttpMessageConverter();

        // 4. 设置支持的内容类型
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        jsonConverter.setSupportedMediaTypes(supportedMediaTypes);
        byteArrayConverter.setSupportedMediaTypes(supportedMediaTypes);

        converters.add(jsonConverter);
        converters.add(byteArrayConverter);

        restTemplate.setMessageConverters(converters);
        return restTemplate;
    }


    /**
     * 催还
     */
    @Override
    public int reminder(BorrowRecordDTO borrowRecordDTO) {
        // 1. 获取借阅记录
        Long recordId = borrowRecordDTO.getId();
        BorrowRecord borrowRecord = borrowRecordMapper.selectBorrowRecordById(recordId);

        // 2. 获取借阅用户信息
        Long borrowUserId = borrowRecord.getUserId();
        SysUser borrowUser = userService.selectUserById(borrowUserId);
        if (borrowUser == null) {
            throw new RuntimeException("借阅用户不存在");
        }

        // 3. 发送系统内部催还消息
        reminderMessageService.sendReminderMessage(
                borrowUserId,
                "借阅逾期归还提醒",
                "借阅已逾期，请及时归还！",
                null,
                null
        );
        System.out.println("已发送系统内部催还消息给用户: " + borrowUserId);

        // 4. 检查用户是否有上级平台ID
        if (borrowUser.getRemark() == null || borrowUser.getRemark().isEmpty()) {
            System.out.println("用户 " + borrowUserId + " 无上级平台ID，跳过上级平台消息发送");
            return 1;
        }

        String platformUserId = borrowUser.getRemark();
        System.out.println("用户 " + borrowUserId + " 在上级平台的ID为: " + platformUserId);

        // 5. 发送催还消息到上级平台
        RestTemplate restTemplate = createRestTemplate(); // 复用相同的RestTemplate配置

        try {
            // 6. 获取平台Token
            String tokenUrl = "http://192.168.1.11:20201/fencing5/api/oauth/token";
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> tokenBody = new HashMap<>();
            tokenBody.put("client_id", "abc");
            tokenBody.put("client_secret", "cba");
            tokenBody.put("grant_type", "client");

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                    tokenUrl,
                    new HttpEntity<>(tokenBody, tokenHeaders),
                    Map.class
            );

            if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
                throw new RuntimeException("获取平台Token失败");
            }

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // 7. 发送催还消息到上级平台
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("access-token", accessToken);

            // 关键修改：处理entityId长度问题
            String entityId = String.valueOf(recordId);
            if (entityId.length() > 32) {
                // 方案1：截取前32个字符
                entityId = entityId.substring(0, 32);
                System.out.println("recordId超过32字符，截取前32位: " + entityId);

                // 方案2：使用哈希值（如果截取不可行）
                // entityId = String.valueOf(recordId.hashCode());
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("appId", "档案系统");
            requestBody.put("appName", "智慧档案管理系统");
            requestBody.put("senderId", "system");
            requestBody.put("receiveIds", Collections.singletonList(platformUserId)); // 只有一个接收者
            requestBody.put("content", "您的档案借阅已逾期，请及时归还！");
            requestBody.put("entityId", entityId); // 使用处理后的ID
            requestBody.put("url", "/manage/record/detail/" + recordId); // 详情链接

            // 调试信息
            System.out.println("催还消息请求URL: http://192.168.1.11:20211/msg/api/open/msg/v2/send");
            System.out.println("催还消息请求体: " + requestBody);

            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://192.168.1.11:20211/msg/api/open/msg/v2/send",
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );

            // 处理响应
            System.out.println("催还消息响应状态: " + response.getStatusCode());
            System.out.println("催还消息响应体: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("上级平台催还消息发送成功");
            } else {
                System.out.println("上级平台催还消息发送失败");
            }
        } catch (Exception e) {
            System.err.println("上级平台催还消息发送异常: " + e.getMessage());
            e.printStackTrace();
        }

        return 1; // 内部消息已发送，返回成功
    }

    /**
     * 催办，我们需要提前检验是否可以被催办，催办通过 业务键查找对应的任务组或者任务候选人，根据用户信息发送催办信息
     * @param id
     * @return
     */
    @Override
    public int expedite(Long id) {
        // 1. 查询借阅记录
        BorrowRecord borrowRecord = borrowRecordMapper.selectBorrowRecordById(id);
        if (!borrowRecord.getStatus().contains("审批")) {
            throw new RuntimeException("流程已结束，无法催办");
        }
        String businessKey = borrowRecord.getBusinessKey();

        // 2. 获取候选用户（审核员）
        List<String> result = taskList.getCandidateUsers(businessKey);
        List<SysUserPost> userPostList = new ArrayList<>();
        result.forEach(userPost -> {
            List<SysPost> sysPosts = sysPostMapper.selectPostList(new SysPost(userPost));
            Long postID = sysPosts.get(0).getPostId();
            userPostList.addAll(sysUserPostMapper.selectUserIdByPostCode(postID));
        });

        // 3. 给所有候选审核员发送系统内部提醒
        for (SysUserPost userPost : userPostList) {
            Long userId = userPost.getUserId();

            // 发送系统内部提醒
            reminderMessageService.sendReminderMessage(
                    userId,
                    "借阅申请审批提醒",
                    "有借阅申请需要审批，请及时处理！",
                    null,  // taskId
                    null   // processInstanceId
            );

            System.out.println("已发送系统内部提醒消息给审核员: " + userId);
        }

        // 4. 筛选在上级平台有账号的审核员
        List<String> platformUserIds = new ArrayList<>();
        for (SysUserPost userPost : userPostList) {
            Long userId = userPost.getUserId();

            // 查询用户详细信息
            SysUser user = userService.selectUserById(userId);
            if (user != null && user.getRemark() != null && !user.getRemark().isEmpty()) {
                // 关键：用户已经是审核员（通过岗位筛选），只需检查remark字段
                platformUserIds.add(user.getRemark());
                System.out.println("审核员 " + userId + " 在上级平台的ID为: " + user.getRemark());
            }
        }

        // 如果没有符合条件的用户，直接返回成功（内部消息已发送）
        if (platformUserIds.isEmpty()) {
            System.out.println("没有在上级平台有账号的审核员，跳过上级平台消息发送");
            return 1;
        }

        // 5. 配置RestTemplate（用于上级平台消息）
        RestTemplate restTemplate = new RestTemplate();
        // 关键修复：添加完整的消息转换器
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        // 1. 添加UTF-8编码的字符串转换器
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false); // 防止添加不必要的charset参数
        converters.add(stringConverter);

        // 2. 添加JSON转换器
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();

        // 3. 添加二进制转换器处理octet-stream
        ByteArrayHttpMessageConverter byteArrayConverter = new ByteArrayHttpMessageConverter();

        // 4. 设置支持的内容类型
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        jsonConverter.setSupportedMediaTypes(supportedMediaTypes);
        byteArrayConverter.setSupportedMediaTypes(supportedMediaTypes);

        converters.add(jsonConverter);
        converters.add(byteArrayConverter);

        restTemplate.setMessageConverters(converters);

        try {
            // 6. 获取平台Token
            String tokenUrl = "http://192.168.1.11:20201/fencing5/api/oauth/token";
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> tokenBody = new HashMap<>();
            tokenBody.put("client_id", "abc");
            tokenBody.put("client_secret", "cba");
            tokenBody.put("grant_type", "client");

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                    tokenUrl,
                    new HttpEntity<>(tokenBody, tokenHeaders),
                    Map.class
            );

            if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
                throw new RuntimeException("获取平台Token失败");
            }

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // 7. 发送催办消息到上级平台
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("access-token", accessToken);

            // 处理entityId长度问题
            String entityId = businessKey;

            // 方案1：如果businessKey是UUID，移除连字符
            if (businessKey != null && businessKey.length() == 36 && businessKey.contains("-")) {
                entityId = businessKey.replace("-", "");
                System.out.println("UUID格式businessKey，移除连字符后: " + entityId);
            }
            // 方案2：截取前32个字符
            else if (businessKey != null && businessKey.length() > 32) {
                entityId = businessKey.substring(0, 32);
                System.out.println("businessKey超过32字符，截取前32位: " + entityId);
            }

            // 方案3：如果仍然超过32字符，使用哈希值
            if (entityId.length() > 32) {
                entityId = String.valueOf(businessKey.hashCode());
                System.out.println("使用哈希值作为entityId: " + entityId);
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("appId", "app.top.msg");
            requestBody.put("appName", "档案消息提醒");
            requestBody.put("senderId", "appId");
            requestBody.put("receiveIds", platformUserIds);
            requestBody.put("content", "有借阅申请需要审批，请及时处理！");
            requestBody.put("entityId", entityId); // 使用处理后的ID

            // 关键调试：打印请求详情
            System.out.println("请求URL: http://192.168.1.11:20211/msg/api/open/msg/v2/send");
            System.out.println("请求头: " + headers);
            System.out.println("请求体: " + requestBody);

            // 使用通用类型接收响应
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://192.168.1.11:20211/msg/api/open/msg/v2/send",
                    new HttpEntity<>(requestBody, headers),
                    String.class // 使用String接收响应
            );
            // 关键调试：打印响应详情
            System.out.println("响应状态: " + response.getStatusCode());
            System.out.println("响应头: " + response.getHeaders());
            System.out.println("响应体: " + response.getBody());

            // 8. 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 手动解析JSON响应
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});

                if ("请求成功!".equals(responseMap.get("result"))) {
                    System.out.println("上级平台消息发送成功");
                    return 1;
                } else {
                    System.out.println("上级平台消息发送失败: " + responseMap);
                }
            } else {
                System.out.println("HTTP请求失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // 更详细的错误处理
            System.err.println("上级平台消息发送异常: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }

        return 1; // 内部消息已发送，返回成功
    }


    @Override
    public int advanceReturn(Long Id)
    {
        BorrowRecord borrowRecord = borrowRecordMapper.selectBorrowRecordById(Id);
        borrowRecord.setStatus("已结束");
        borrowRecord.setReturnDate(DateUtils.getNowDate());
        borrowRecordMapper.updateBorrowRecord(borrowRecord);

        return 1;
    }

    private void rfidReturn(String Rfid)
    {
        Long archiveId = archiveMapper.getArchiveIdByRfid(Rfid);
        if (archiveId != null){

            BorrowRecord borrowRecord = borrowRecordMapper.selectBorrowRecordByArchiveId(archiveId);
            if (borrowRecord != null){
                borrowRecord.setStatus("已结束");
                borrowRecord.setReturnDate(DateUtils.getNowDate());
                borrowRecordMapper.updateBorrowRecord(borrowRecord);
            }
        }
    }

    @Override
    public int directBorrow(BorrowRecordDTO borrowRecordDTO)
    {
        Archive archive = archiveMapper.selectArchiveByDanghao(borrowRecordDTO.getArchiveDangHao());
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUserId(SecurityUtils.getUserId());
        borrowRecord.setArchiveId(archive.getId());
        borrowRecord.setStartDate(borrowRecordDTO.getStartDate());
        borrowRecord.setEndDate(borrowRecordDTO.getEndDate());
        borrowRecord.setPurpose(borrowRecordDTO.getPurpose());
        borrowRecord.setProcessName(borrowRecordDTO.getProcessName());
        borrowRecord.setRemark(borrowRecordDTO.getRemark());


        borrowRecord.setBusinessKey(UUID.randomUUID().toString());
        String instanceId = workFlowStarter.startProcessInstanceByDefinitionKey(borrowRecord.getProcessName(), borrowRecord.getBusinessKey(), borrowRecordDTO.getUserName()+"借阅申请");

        borrowRecord.setInstanceId(instanceId);
        borrowRecord.setStartApplyTime(DateUtils.getNowDate());
        borrowRecord.setStatus("待审批");
        return borrowRecordMapper.insertBorrowRecord(borrowRecord);
    }
}
