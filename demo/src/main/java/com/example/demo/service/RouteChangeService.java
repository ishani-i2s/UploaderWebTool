package com.example.demo.service;
import com.example.demo.entity.TaskDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RouteChangeService {
    List<String> getSite(String accessToken);
    List<TaskDetails> getTaskDetails(String accessToken, String site, String status, String plannedStart);
    List<TaskDetails> updateTaskDetails(MultipartFile file, String accessToken);
}
