package com.iams.activiti8.service.impl;

import com.iams.activiti8.domain.dto.ProcessDefinitionDTO;
import com.iams.activiti8.domain.dto.DefinitionIdDTO;
import com.iams.activiti8.domain.vo.ActReDeploymentVO;
import com.iams.activiti8.service.IProcessDefinitionService;
import com.iams.activiti8.mapper.ActReDeploymentMapper;
import com.iams.common.core.page.PageDomain;
import com.iams.common.utils.StringUtils;
import com.iams.common.utils.file.FileUploadUtils;
import com.iams.common.config.RuoYiConfig;

import com.iams.minio.config.MinIOInfo;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;

import com.github.pagehelper.Page;

import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
public class ProcessDefinitionServiceImpl implements IProcessDefinitionService{

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ActReDeploymentMapper actReDeploymentMapper;

    @Autowired
    private RuntimeService runtimeService;

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinIOInfo minioInfo;

//    private final String MAIN_BUCKET = "iams-main-files";

    @Override
    public Page<ProcessDefinitionDTO> selectProcessDefinitionList(ProcessDefinitionDTO processDefinition, PageDomain pageDomain) {
        Page<ProcessDefinitionDTO> list = new Page<>();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionId().orderByProcessDefinitionVersion().desc();
        if(StringUtils.isNotBlank(processDefinition.getName())){
            processDefinitionQuery.processDefinitionNameLike("%"+processDefinition.getName()+"%");
        }
        if(StringUtils.isNotBlank(processDefinition.getKey())){
            processDefinitionQuery.processDefinitionKeyLike("%"+processDefinition.getKey()+"%");
        }
        List<ProcessDefinition> processDefinitions = processDefinitionQuery.listPage((pageDomain.getPageNum() - 1) * pageDomain.getPageSize(), pageDomain.getPageSize());
        long count = processDefinitionQuery.count();
        list.setTotal(count);
        if(count != 0){
            Set<String> ids = processDefinitions.parallelStream().map(pdl -> pdl.getDeploymentId()).collect(Collectors.toSet());



            List<ActReDeploymentVO> actReDeploymentVOS = actReDeploymentMapper.selectActReDeploymentByIds(ids);
            List<ProcessDefinitionDTO> processDefinitionDTOS = processDefinitions.stream()
                    .map(pd->new ProcessDefinitionDTO((ProcessDefinitionEntityImpl) pd, actReDeploymentVOS.parallelStream().filter(ard->pd.getDeploymentId().equals(ard.getId())).findAny().orElse(new ActReDeploymentVO())))
                    .collect(Collectors.toList());
            list.addAll(processDefinitionDTOS);
        }
        return list;
    }

    @Override
    public DefinitionIdDTO getDefinitionsByInstanceId(String instanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        String deploymentId = processInstance.getDeploymentId();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
        return new DefinitionIdDTO(processDefinition);
    }

    @Override
    public int deleteProcessDefinitionById(String id) {
        System.out.println("删除流程定义：" + id);

        // 检查是否有正在运行的实例
        long count = runtimeService.createProcessInstanceQuery()
                .processDefinitionId(id)
                .count();

        if (count > 0) {
            throw new RuntimeException("无法删除流程定义，因为有正在运行的实例。");
        }

        // 删除流程定义
        repositoryService.deleteDeployment(id, false);
        return 1;
    }

    @Override
    public void uploadStreamAndDeployment(MultipartFile file) throws IOException {
        // 获取上传的文件名
        String fileName = file.getOriginalFilename();
        // 得到输入流（字节流）对象
        InputStream fileInputStream = file.getInputStream();
        // 文件的扩展名
        String extension = FilenameUtils.getExtension(fileName);

        if (extension.equals("zip")) {
            ZipInputStream zip = new ZipInputStream(fileInputStream);
            repositoryService.createDeployment()//初始化流程
                    .addZipInputStream(zip)
                    .deploy();
        } else {
            repositoryService.createDeployment()//初始化流程
                    .addInputStream(fileName, fileInputStream)

                    .deploy();
        }
    }

    @Override
    public void suspendOrActiveApply(String id, Integer suspendState) {
        if (1==suspendState) {
            // 当流程定义被挂起时，已经发起的该流程定义的流程实例不受影响（如果选择级联挂起则流程实例也会被挂起）。
            // 当流程定义被挂起时，无法发起新的该流程定义的流程实例。
            // 直观变化：act_re_procdef 的 SUSPENSION_STATE_ 为 2
            repositoryService.suspendProcessDefinitionById(id);
        } else if (2==suspendState) {
            repositoryService.activateProcessDefinitionById(id);
        }
    }


    @Override
    public String upload(MultipartFile multipartFile) throws IOException {
        return FileUploadUtils.upload(RuoYiConfig.getUploadPath()+"/processDefinition" , multipartFile);
    }

    @Override
    public void addDeploymentByString(String stringBPMN) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(stringBPMN)));
            
            // 获取process元素的id属性
            NodeList processList = doc.getElementsByTagName("bpmn2:process");
            String processId = "";
            if (processList.getLength() > 0) {
                Element processElement = (Element) processList.item(0);
                processId = processElement.getAttribute("id");
            }
            
            // 使用流程ID作为文件名
            String fileName = processId + ".bpmn";
            
            // 部署流程
            repositoryService.createDeployment()
                    .addString(fileName, stringBPMN)
                    .name(processId + "流程定义")
                    .deploy();
                    
        } catch (Exception e) {
            throw new RuntimeException("部署流程失败: " + e.getMessage());
        }
    }

    @Override
    public void getProcessDefineXML(HttpServletResponse response, String deploymentId, String resourceName) throws IOException {
        InputStream inputStream = repositoryService.getResourceAsStream(deploymentId, resourceName);
        int count = inputStream.available();
        byte[] bytes = new byte[count];
        response.setContentType("text/xml");
        OutputStream outputStream = response.getOutputStream();
        while (inputStream.read(bytes) != -1) {
            outputStream.write(bytes);
        }
        inputStream.close();
    }
}
