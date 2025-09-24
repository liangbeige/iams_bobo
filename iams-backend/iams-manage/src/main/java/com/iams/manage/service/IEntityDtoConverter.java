package com.iams.manage.service;

import com.iams.manage.domain.TaskDTO.ArchiveTaskDTO;


public interface IEntityDtoConverter<E, D> {

    // 将 DTO 转换为 Entity
    E dtoToEntity(D dto);

    // 将 Entity 转换为 DTO
    D entityToDto(E entity);

    // 将 Entity 转换为 TaskDTO，并非转换，实为填充信息
     public ArchiveTaskDTO taskInfoCompletion(ArchiveTaskDTO archiveTaskDTO);

}
