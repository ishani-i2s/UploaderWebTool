package com.example.demo.service;

import com.example.demo.entity.FixedAsset;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FixedAssetsService {
    List<FixedAsset> save(MultipartFile file,String accessToken);
    List<FixedAsset> findAll();
}
