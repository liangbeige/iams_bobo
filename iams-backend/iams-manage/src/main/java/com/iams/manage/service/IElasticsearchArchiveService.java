package com.iams.manage.service;

import com.iams.elasticsearch.domain.ElasticsearchArchive;

import java.util.List;
import java.util.Map;

public interface IElasticsearchArchiveService {


    public List<ElasticsearchArchive> CombineSearch(Map<String, String> combineParams);


    long getTotalCount(Map<String, String> allParams);
}
