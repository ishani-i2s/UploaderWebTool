package com.example.demo.service;

import com.example.demo.entity.FunctionalObject;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FunctionalObjectService {
    List<FunctionalObject> save(MultipartFile file,String accessToken);
    List<FunctionalObject> findAll();
    List<FunctionalObject> getAll(List<Integer> ids, String accessToken);
}
