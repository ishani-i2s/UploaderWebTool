package com.example.demo.service.Impl;
import com.example.demo.controller.ExcelHelper;
import com.example.demo.entity.TaskDetails;
import com.example.demo.service.RouteChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class routeChangesImpl implements RouteChangeService {
    String baseURL="https://ifscloud.tsunamit.com";
    StringBuilder stringBuilder=new StringBuilder(baseURL);

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<String> getSite(String accessToken) {
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        String url=stringBuilder.append(baseURL).append("/main/ifsapplications/projection/v1/WorkTaskHandling.svc/Reference_UserAllowedSiteLov").toString();
        System.out.println("The url is"+ url);
        try {
            ResponseEntity<Map> responseEntity=restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println(responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> siteList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                siteList.add(map.get("Contract").toString()+"-"+map.get("ContractDesc").toString());
            }
            System.out.println("The site list is"+siteList);
            return siteList;
        }catch (Exception e){
            System.out.println("Failed to get site list");
            return null;
        }
    }

    @Override
    public List<TaskDetails> getTaskDetails(String accessToken, String site, String status, String plannedStart) {
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);

        //convert the status into uppercase
        status=status.toUpperCase();

        //convert the planned start into date format
        String todayT00= new StringBuilder().append(plannedStart).append("T00:00:00Z").toString();
        String todayT23= new StringBuilder().append(plannedStart).append("T23:59:59Z").toString();

        String url=stringBuilder.append(baseURL).append("/main/ifsapplications/projection/v1/WorkTaskHandling.svc/JtTaskSet?$filter=(((Objstate eq IfsApp.WorkTaskHandling.JtTaskState'").append(status).append("') and Site eq '").append(site).append("') and PlannedStart ge ").append(todayT00).append(" and PlannedStart le ").append(todayT23).append(")").toString();
        System.out.println("The url is "+ url);
        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println(responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<TaskDetails> taskDetailsList = new ArrayList<>();
            responseList.forEach(map -> {
                TaskDetails taskDetails = new TaskDetails();
                System.out.println("task id is"+map.get("TaskSeq"));
                System.out.println("description is"+map.get("Description"));
                System.out.println("site is"+map.get("Site"));
                System.out.println("planned start is"+map.get("PlannedStart"));
                System.out.println("status is"+map.get("Objstate"));
                System.out.println("object id is"+map.get("ActualObjectId"));
                System.out.println("work type is"+map.get("WorkTypeId"));
                taskDetails.setTaskId(map.get("TaskSeq").toString());
                taskDetails.setDescription((String)map.get("Description"));
                taskDetails.setSite((String)map.get("Site"));
                taskDetails.setPlannedStartDate((String)map.get("PlannedStart"));
                taskDetails.setStatus((String)map.get("Objstate"));
                taskDetails.setObjectId((String)map.get("ActualObjectId"));
                taskDetails.setWorkType((String)map.get("WorkTypeId"));
                taskDetailsList.add(taskDetails);
            });
            System.out.println("The task details list is"+taskDetailsList);
            return taskDetailsList;
        }catch (Exception e){
            System.out.println("Failed to get task details");
            return null;
        }
    }

    @Override
    public List<TaskDetails> updateTaskDetails(MultipartFile file, String accessToken) {
        List<TaskDetails> taskDetailsList = new ArrayList<>();
        try {
            List<TaskDetails> taskDetails = ExcelHelper.readTaskDetails(file.getInputStream());
            System.out.println("The task details in update are"+taskDetails);
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpHeaders gethttpHeaders(String accessToken) {
        HttpHeaders headers= new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }
}
