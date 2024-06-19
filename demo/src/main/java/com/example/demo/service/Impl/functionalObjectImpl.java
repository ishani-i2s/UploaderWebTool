package com.example.demo.service.Impl;
import com.example.demo.entity.FunctionalObject;
import com.example.demo.repo.FunctionalObjectRepo;
import com.example.demo.service.FunctionalObjectService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.controller.ExcelHelper;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;


@Service
public class functionalObjectImpl implements FunctionalObjectService {
    String baseURL="https://ifscloud.tsunamit.com";
    StringBuilder stringBuilder=new StringBuilder(baseURL);
//    String accessToken= "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJVdWtEM044dVFiMkgyOGZBNFRnWGh4b1JmMElXMUNkTXV0cjlLbDRKbmpJIn0.eyJleHAiOjE3MTUxNTUxNTEsImlhdCI6MTcxNTE1MTU1MSwiYXV0aF90aW1lIjoxNzE1MTUxNTQ5LCJqdGkiOiJiNTkxZmNiMS00ZjlkLTRjZTYtYmU3Zi02YzJkY2ZjYTFiYmIiLCJpc3MiOiJodHRwczovL2lmc2Nsb3VkLnRzdW5hbWl0LmNvbS9hdXRoL3JlYWxtcy90c3V0c3QiLCJhdWQiOlsidHN1dHN0IiwiYWNjb3VudCJdLCJzdWIiOiJmMjJhOTYwNy04NzNjLTRjZWYtOGEzMi0xODE5NjdlMWRmZjUiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJJRlNfYXVyZW5hIiwic2Vzc2lvbl9zdGF0ZSI6IjkwMjkyNDk5LTgyN2UtNDUwOC05ZjlkLTYxZTdkNjFhYWMxOSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovLyIsImh0dHBzOi8vaWZzLWFwcC5naDRzdnF3NXQydXUzbDJpeXRiMWhnZXNnYi5ieC5pbnRlcm5hbC5jbG91ZGFwcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImRlZmF1bHQtcm9sZXMtdHN1dHN0Iiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgbWljcm9wcm9maWxlLWp3dCBlbWFpbCBhdWRpZW5jZSIsInNpZCI6IjkwMjkyNDk5LTgyN2UtNDUwOC05ZjlkLTYxZTdkNjFhYWMxOSIsInVwbiI6Im5hZGVlc2hhbiIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZ3JvdXBzIjpbImRlZmF1bHQtcm9sZXMtdHN1dHN0Iiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJuYWRlZXNoYW4iLCJlbWFpbCI6Im5hZGVlc2hhLm5AY3JlYXRpdmVzb2Z0d2FyZS5jb20ifQ.U2jGmc2Vl9Z9ZD-f4RrBt7hVNzoXyR8H6QtG3c9gjJbp25bbzq4XmTN7FKGraZIVe_9_dsFSZStJxPA8lr6zVICqQZwCSGclEidrB7fViPLn0ouFAcXpdoxi6WAeGAJS0iL7q2aJ43ida0NPr4ECXuPCwYrRCQHaoV733r_zQPgY053FiXySdA0_kcBxCv3henf7qsOKZqh_ElEKZZwJYnb2AH-agMoLd6m6pqfe4mcrYq2dIrKFM4PV8QALxnCMR0TZ6xBPjdcJ6B__zGmBL6WW6ktnXNfiuCPI6_TAzhdZR49RGLcliPpcNPikKi0PY5GFhi-DXFkFl-s44rtzxg";
    @Autowired
    FunctionalObjectRepo functionalObject;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<FunctionalObject> save(MultipartFile file, String accessToken) {
        List<FunctionalObject> errorList = new ArrayList<>();
        try{
//            System.out.println("Inside the save method");
            List<FunctionalObject> funList = ExcelHelper.excelToFunList(file.getInputStream());

            List<FunctionalObject> notNullErrorSet = new ArrayList<>();
            //not null check
            funList.forEach(
                    fun -> {
                        if(fun.getSite()==null || fun.getObjectId()==null || fun.getObjLevel()==null) {
                            fun.setLog("Site, Object Id and ObjLevel cannot be null");
                            notNullErrorSet.add(fun);
                        }
                    }
            );

            //add the null objects to the error list
            errorList.addAll(notNullErrorSet);

            //remove the not null objects from the list
            funList.removeAll(notNullErrorSet);

            //get the valid objLevel from an api call
            errorList.addAll(checkObjLevel(funList,accessToken));

            //remove the invalid objLevel from the list
            funList.removeAll(errorList);

            //validation for the site
            errorList.addAll(checkSite(funList,accessToken));

            //remove the invalid site from the list
            funList.removeAll(errorList);

            //validation for the item class
            errorList.addAll(checkItemClass(funList,accessToken));

            //remove the invalid item class from the list
            funList.removeAll(errorList);

            //validation for part no
            errorList.addAll(checkPartNo(funList,accessToken));

            //remove the invalid part no from the list
            funList.removeAll(errorList);

            //validation for belong to object (same as object id)
            errorList.addAll(checkBelongToObject(funList,accessToken));

            //remove the invalid belong to object from the list
            funList.removeAll(errorList);

            //validation for the existing belong to object
            errorList.addAll(checkBelongToObjectExist(funList,accessToken));

            //remove the invalid belong to object from the list
            funList.removeAll(errorList);

            //validation for the location id
            errorList.addAll(checkLocationId(funList,accessToken));

            //remove the invalid location id from the list
            funList.removeAll(errorList);

            //valdation for the party type and party identity
            errorList.addAll(checkPartyTypeAndPartyIdentity(funList,accessToken));

            //remove the invalid party type and party identity from the list
            funList.removeAll(errorList);

            //validation for work Type
            errorList.addAll(checkWorkType(funList,accessToken));

            //remove the invalid work type from the list
            funList.removeAll(errorList);

            //validation for calender
            errorList.addAll(checkCalender(funList,accessToken));

            //remove the invalid calender from the list
            funList.removeAll(errorList);


            Map<String,Object> defaultValues = getDefaultValues(accessToken);

            //validate the fixed asset
            errorList.addAll(checkFixedAsset(funList,accessToken,defaultValues));

            //remove the invalid fixed asset from the list
            funList.removeAll(errorList);

            //save the valid functional objects to the database
            functionalObject.saveAll(funList);

            if(funList.size()==0){
                return errorList;
            }else{
                List<FunctionalObject> postStatus= postFunctionalObject(funList,accessToken);
                System.out.println("The post status is"+postStatus);
                if(postStatus.size()>0){
                    errorList.addAll(postStatus);
                    return errorList;
                }else {
                    return errorList;
                }
            }
        }catch (Exception e) {
              return errorList;
        }
    }

    private List<FunctionalObject> postFunctionalObject(List<FunctionalObject> funList,String accessToken) {
//        System.out.println("Inside the postFunctionalObject method");
//        System.out.println("The functional object list is"+funList);
        //get the default values for the functional object
        Map<String,Object> defaultValues = getDefaultValues(accessToken);

        HttpHeaders headers= gethttpHeaders(accessToken);
        stringBuilder.setLength(0);
        String url = stringBuilder.append(baseURL).append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/EquipmentFunctionalSet").toString();
        System.out.println("The url is"+url);
        List<FunctionalObject> invalidList= new ArrayList<>();
        int i=0;
        for (FunctionalObject fun : funList) {
            System.out.println("Step "+i++);
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("Company", defaultValues.get("Company"));
            payload.put("Contract", fun.getSite());
            payload.put("MchCode", fun.getObjectId());
            payload.put("MchName", fun.getDescription());
            payload.put("SupContract", fun.getSite());
            payload.put("PmProgApplicationStatus", defaultValues.get("PmProgApplicationStatus"));
            payload.put("IsCategoryObject", defaultValues.get("IsCategoryObject"));
            payload.put("IsGeographicObject", defaultValues.get("IsGeographicObject"));
            payload.put("SafetyCriticalElement", false);
            payload.put("SafeAccessCode", defaultValues.get("SafeAccessCode"));
            payload.put("ObjLevel", fun.getObjLevel());
            payload.put("ItemClassId", fun.getItemClass());
            payload.put("LocationId", fun.getLocationId());
            payload.put("Note", fun.getNote());
            payload.put("ProductionDate", fun.getInstallationDate());
            payload.put("SerialNo", fun.getSerialNo());
            payload.put("SupMchCode", fun.getBelongToObject());
            payload.put("PartNo", fun.getPartNo());
            payload.put("ObjectCodePart","F");
            payload.put("ObjectNo", fun.getFixedAsset());
            HttpEntity<Map> httpEntity = new HttpEntity<>(payload, headers);
            System.out.println("The Post Call payload is"+payload);
            System.out.println("HttpEntity is"+httpEntity);
            try{
                ResponseEntity<Map> response= restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
                if(response.getStatusCode().toString().equals("201 CREATED")){
                    //get the EquipmentObjectSeq value from the response
                    Map<String, Object> responseMap = response.getBody();
                    System.out.println("The response map is"+responseMap);
                    String equipmentObjectSeq = responseMap.get("EquipmentObjectSeq").toString();
                    System.out.println("Functional Object created successfully");
                    //add party type and party identity
                    Boolean resultParty=PostPartyTypeAndPartyIdentity(fun,accessToken,equipmentObjectSeq);
                    if(resultParty){
                        Boolean resultWorkType=PostWorkType(fun,accessToken,equipmentObjectSeq);
                        if(resultWorkType) {
                            fun.setLog("Successfull");
                        }else {
                            fun.setLog("Failed to post work type");
                        }
                    }else{
                        fun.setLog("Failed to post party type and party identity");
                    }
                    invalidList.add(fun);
                    continue;
                }else {
                    fun.setLog("Failed to create functional object");
                    invalidList.add(fun);
                }
            }catch (Exception e) {
                String errorResponse = e.getMessage();
                String errorMessage = extractErrorMessageFromJson(errorResponse);
                fun.setLog(errorMessage);
                invalidList.add(fun);
            }
        }
        System.out.println("The invalid list is"+invalidList);
        System.out.println("Posted all the functional objects");
        return invalidList;
    }

    private Boolean PostWorkType(FunctionalObject fun, String accessToken, String equipmentObjectSeq) {
        HttpHeaders headers= gethttpHeaders(accessToken);
        stringBuilder.setLength(0);
        String url = stringBuilder.append(baseURL).append("/main/ifsapplications/projection/v1/SvcschFunctionalObjectSchedulingDetailHandling.svc/FunctionalObjectDetailSet(EquipmentObjectSeq=").append(equipmentObjectSeq).append(")/ObjectAvailabilityDetailArray").toString();
        Map<String, Object> payloadWorkType = new java.util.HashMap<>();
        int equipmentObjectSeqInt = Integer.parseInt(equipmentObjectSeq);
        payloadWorkType.put("CalendarId", fun.getCalender());
        payloadWorkType.put("EquipmentObjectSeq", equipmentObjectSeqInt);
        payloadWorkType.put("WorkType", fun.getWorkType());
        HttpEntity<Map> httpEntityWorkType = new HttpEntity<>(payloadWorkType, headers);
        System.out.println("The payload work type is"+payloadWorkType);
        System.out.println("The httpEntity work type is"+httpEntityWorkType);
        try{
            var responseWorkType= restTemplate.exchange(url, HttpMethod.POST, httpEntityWorkType, Map.class);
            if(responseWorkType.getStatusCode().toString().equals("201 CREATED")){
                System.out.println("Work Type posted successfully");
                return true;
            }else {
                System.out.println("Work Type POST API call failed");
                return false;
            }
        }catch (Exception e) {
            String errorResponse = e.getMessage();
            String errorMessage = extractErrorMessageFromJson(errorResponse);
            System.out.println("The error message is"+errorMessage);
            return false;
        }
    }

    private String getDeliveryForCustomers(FunctionalObject fun,String accessToken){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/GetDeliveryAddress(CustomerNo='").append(fun.getPartyIdentity()).append("')").toString();
        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The delivery address response map is" + responseMap);
            String deliveryAddress = (String) responseMap.get("value");
            return deliveryAddress;
        }catch (Exception e){
            System.out.println("The delivery address API call failed");
            return null;
        }
    }

    private Boolean PostPartyTypeAndPartyIdentity(FunctionalObject fun, String accessToken, String equipmentObjectSeq) {
        Map<String, Object> payloadParty = new java.util.HashMap<>();
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String urlParty=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/EquipmentFunctionalSet(EquipmentObjectSeq=").append(equipmentObjectSeq).append(")/EquipmentObjectPartyArray").toString();
        if(fun.getPartyType().equals("CUSTOMER")) {
            payloadParty.put("DeliveryAddress", getDeliveryForCustomers(fun, accessToken));
        }

        String partyType = fun.getPartyType().substring(0, 1).toUpperCase() + fun.getPartyType().substring(1).toLowerCase();
        int equipmentObjectSeqInt = Integer.parseInt(equipmentObjectSeq);
        payloadParty.put("PartyType", partyType);
        payloadParty.put("Identity", fun.getPartyIdentity());
        payloadParty.put("EquipmentObjectSeq", equipmentObjectSeqInt);
        payloadParty.put("IsPrimaryDb", false);
        HttpEntity<Map> httpEntityParty = new HttpEntity<>(payloadParty, gethttpHeaders(accessToken));
        System.out.println("The payload party is"+payloadParty);
        System.out.println("The httpEntity party is"+httpEntityParty);
        try{
            var responseParty= restTemplate.exchange(urlParty, HttpMethod.POST, httpEntityParty, Map.class);
            if(responseParty.getStatusCode().toString().equals("201 CREATED")){
                System.out.println("Party Type and Party Identity posted successfully");
                return true;
            }else {
                System.out.println("Party Type and Party Identity POST API call failed");
                return false;
            }
        }catch (Exception e) {
            String errorResponse = e.getMessage();
            String errorMessage = extractErrorMessageFromJson(errorResponse);
            System.out.println("The error message is"+errorMessage);
            return false;
        }
    }

    private Map<String, Object> getDefaultValues(String accessToken) {
        HttpHeaders headers= gethttpHeaders(accessToken);
        stringBuilder.setLength(0);
        String url = stringBuilder.append(baseURL).append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/EquipmentFunctionalSet/IfsApp.FunctionalObjectHandling.EquipmentFunctional_Default()").toString();
        HttpEntity<Void> httpEntity= new HttpEntity<>(headers);
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The default values response map is" + responseMap);
            System.out.println("Company is"+responseMap.get("Company"));
            return responseMap;
        } catch (Exception e){
            System.out.println("The default values API call failed");
            return null;
        }
    }

    private String extractErrorMessageFromJson(String errorResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(errorResponse.substring(errorResponse.indexOf("{")));
            System.out.println("The root node is"+rootNode);
            JsonNode errorMessageNode = rootNode.path("error").path("message");
            return errorMessageNode.asText();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error occurred while parsing JSON response.";
        }
    }

    private List<FunctionalObject> checkObjLevel(List<FunctionalObject> funList,String accessToken) {
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/Reference_EquipmentObjectLevel").toString();
        System.out.println("The url is"+url);
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
//            System.out.println("The response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> objLevelList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                objLevelList.add((String) map.get("ObjLevel"));
            }
            List<FunctionalObject> invalidList = new ArrayList<>();
            for (FunctionalObject fun : funList) {
                if (!objLevelList.contains(fun.getObjLevel())) {
                    fun.setLog("Invalid ObjLevel");
                    invalidList.add(fun);
                }
            }

            //remove the invalid objLevel from the list
            funList.removeAll(invalidList);

            //compare the level seq of the objLevel and belong to object
            String objLevelSeq = null;
            String belongToObjectSeq = null;
            if(funList.size()!=0){
                for (FunctionalObject fun : funList) {
                    String objLevel = fun.getObjLevel();
                    String belongToObjLevel= null;

                    //get objLevel of belong to object
                    stringBuilder.setLength(0);
                    stringBuilder.append(baseURL);
                    String urlBelongToObject = stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/EquipmentFunctionalSet?$select=EquipmentObjectSeq,MchCode,ObjLevel,OperationalStatus").toString();
                    try {
                        ResponseEntity<Map> responseEntityBelongToObject = restTemplate.exchange(urlBelongToObject, HttpMethod.GET, httpEntity, Map.class);
                        Map<String, Object> responseMapBelongToObject = responseEntityBelongToObject.getBody();
                        List<Map<String, Object>> responseListBelongToObject = (List<Map<String, Object>>) responseMapBelongToObject.get("value");
                        for (Map<String, Object> map : responseListBelongToObject) {
                            if (map.get("MchCode").equals(fun.getBelongToObject())) {
                                belongToObjLevel = map.get("ObjLevel").toString();
                                System.out.println("The belongToObjectLevel is" + belongToObjectSeq);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("The belong to object API call failed");
                        fun.setLog("API call failed from belong to object");
                        invalidList.add(fun);
                    }
                    for (Map<String, Object> map : responseList) {
                        if (map.get("ObjLevel").equals(objLevel)) {
                            objLevelSeq = map.get("LevelSeq").toString();
                            System.out.println("The objLevelSeq is" + objLevelSeq);
                        }
                        if (map.get("ObjLevel").equals(belongToObjLevel)) {
                            belongToObjectSeq = map.get("LevelSeq").toString();
                            System.out.println("The belongToObjectSeq is" + belongToObjectSeq);
                        }
                    }
                    if(Integer.parseInt(objLevelSeq)<=Integer.parseInt(belongToObjectSeq)){
                        fun.setLog("ObjLevel cannot be less than belong to object");
                        invalidList.add(fun);
                    }
                }
            }
            return invalidList;
        } catch (Exception e){
            List<FunctionalObject> invalidList = new ArrayList<>();
            for(FunctionalObject fun: funList){
                fun.setLog("API call failed");
                invalidList.add(fun);
            }
            return invalidList;
        }
    }

    private List<FunctionalObject> checkSite(List<FunctionalObject> funList,String accessToke){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToke));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/Reference_UserAllowedSiteLov").toString();
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The site response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> siteList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                siteList.add((String) map.get("Contract"));
            }
            List<FunctionalObject> invalidList = new ArrayList<>();
            for (FunctionalObject fun : funList) {
                if (!siteList.contains(fun.getSite())) {
                    fun.setLog("Invalid Site");
                    invalidList.add(fun);
                }
            }
            return invalidList;
        } catch (Exception e){
            List<FunctionalObject> invalidList = new ArrayList<>();
            for(FunctionalObject fun: funList){
                fun.setLog("API call failed");
                invalidList.add(fun);
            }
            return invalidList;
        }
    }

    private List<FunctionalObject> checkItemClass(List<FunctionalObject> funList,String accessToken){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/Reference_ItemClass?$filter=(Objstate eq IfsApp.FunctionalObjectHandling.ItemClassState'Active')").toString();
        System.out.println("The item class url is"+url);
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The item class response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> itemClassList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                itemClassList.add((String) map.get("ItemClassId"));
            }
            List<FunctionalObject> invalidList = new ArrayList<>();
            for (FunctionalObject fun : funList) {
                if (!itemClassList.contains(fun.getItemClass())) {
                    fun.setLog("Invalid Item Class");
                    invalidList.add(fun);
                }
            }
            return invalidList;
        } catch (Exception e){
            System.out.println("The item class API call failed");
            List<FunctionalObject> invalidList = new ArrayList<>();
            for(FunctionalObject fun: funList){
                fun.setLog("API call failed from item");
                invalidList.add(fun);
            }
            return invalidList;
        }
    }

    private List<FunctionalObject> checkPartNo(List<FunctionalObject> funList,String accessToken){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/GetPartNos()?$skip=0&$top=101").toString();
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The part no response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> partNoList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                partNoList.add((String) map.get("PartNo"));
            }
            List<FunctionalObject> invalidList = new ArrayList<>();
            for (FunctionalObject fun : funList) {
                if (!partNoList.contains(fun.getPartNo())) {
                    fun.setLog("Invalid Part No");
                    invalidList.add(fun);
                }
            }
            return invalidList;
        } catch (Exception e){
            System.out.println("The part no API call failed");
            List<FunctionalObject> invalidList = new ArrayList<>();
            for(FunctionalObject fun: funList){
                fun.setLog("API call failed from part no");
                invalidList.add(fun);
            }
            return invalidList;
        }
    }

    private List<FunctionalObject> checkBelongToObject(List<FunctionalObject> funList,String accessToken){
         //check the belong to object is same as the object id
        List<FunctionalObject> invalidList = new ArrayList<>();
        for (FunctionalObject fun : funList) {
            if (fun.getBelongToObject().equals(fun.getObjectId())) {
                fun.setLog("Belong to object cannot be same as object id");
                invalidList.add(fun);
            }
        }
        return invalidList;
    }

    private List<FunctionalObject> checkBelongToObjectExist(List<FunctionalObject> funList,String accessToken){
        //check the belong to object exists
        List<FunctionalObject> invalidList = new ArrayList<>();
        for(FunctionalObject fun:funList){
            String contract = fun.getSite();
            HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
            stringBuilder.setLength(0);
            stringBuilder.append(baseURL);
            String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/Reference_EquipmentFunctional?$filter=(Contract eq '").append(contract).append("')").toString();
            try {
                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap = responseEntity.getBody();
                System.out.println("The belong to object response map is" + responseMap);
                List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
                List<String> objectIdList = new ArrayList<>();
                for (Map<String, Object> map : responseList) {
                    objectIdList.add((String) map.get("MchCode"));
                }
                if (!objectIdList.contains(fun.getBelongToObject())) {
                    fun.setLog("Belong to object does not exist");
                    invalidList.add(fun);
                }
            } catch (Exception e){
                System.out.println("The belong to object API call failed");
                fun.setLog("API call failed from belong to object");
                invalidList.add(fun);
            }
        }
        return invalidList;
    }

    private List<FunctionalObject> checkLocationId(List<FunctionalObject> funList,String accessToken){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/LocationFilterFunction()").toString();
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The location id response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> locationIdList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                locationIdList.add((String) map.get("LocationId"));
            }
            List<FunctionalObject> invalidList = new ArrayList<>();
            for (FunctionalObject fun : funList) {
                if (!locationIdList.contains(fun.getLocationId())) {
                    fun.setLog("Invalid Location Id");
                    invalidList.add(fun);
                }
            }
            return invalidList;
        } catch (Exception e){
            System.out.println("The location id API call failed");
            List<FunctionalObject> invalidList = new ArrayList<>();
            for(FunctionalObject fun: funList){
                fun.setLog("API call failed from location id");
                invalidList.add(fun);
            }
            return invalidList;
        }
    }

    private List<FunctionalObject> checkPartyTypeAndPartyIdentity(List<FunctionalObject> funList,String accessToken){
        List<FunctionalObject> errorList = new ArrayList<>();
        for (FunctionalObject fun : funList) {
            if (fun.getPartyType().equals("CUSTOMER")) {
                //validate customer
                errorList=checkCustomerPartyIdentity(fun,accessToken);
            } else if (fun.getPartyType().equals("SUPPLIER")) {
                //validate supplier
                errorList=checkSupplierPartyIdentity(fun,accessToken);
            } else if (fun.getPartyType().equals("CONTRACTOR")) {
                errorList=checkSupplierPartyIdentity(fun,accessToken);
                //validate contractor
            } else if (fun.getPartyType().equals("Manufacturer")) {
                //validate manufacturer
                errorList=checkManufacturerPartyIdentity(fun,accessToken);
            } else if(fun.getPartyType().equals("OWNER")){
                //validate owner
                errorList=checkOwnerPartyIdentity(fun,accessToken);
            }else{
                //asset manager
                errorList= checkAssetManagerIdentity(fun,accessToken);
            }
        }
        return errorList;
    }

    private List<FunctionalObject> checkCustomerPartyIdentity(FunctionalObject fun,String accessToken){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/Reference_CustomerInfoCustcategoryPub?$skip=0&$top=101").toString();
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The customer party identity response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> partyIdentityList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                partyIdentityList.add((String) map.get("CustomerId"));
            }
            if (!partyIdentityList.contains(fun.getPartyIdentity())) {
                fun.setLog("Invalid Party Identity");
                return List.of(fun);
            }
            return List.of();
        } catch (Exception e){
            System.out.println("The party identity API call failed");
            fun.setLog("API call failed from party identity");
            return List.of(fun);
        }
    }

    private List<FunctionalObject> checkSupplierPartyIdentity(FunctionalObject fun,String accessToken){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/Reference_SupplierInfo?$skip=0&$top=101").toString();
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The supplier party identity response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> partyIdentityList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                partyIdentityList.add((String) map.get("SupplierId"));
            }
            if (!partyIdentityList.contains(fun.getPartyIdentity())) {
                fun.setLog("Invalid Party Identity");
                return List.of(fun);
            }
            return List.of();
        } catch (Exception e){
            System.out.println("The party identity API call failed");
            fun.setLog("API call failed from party identity");
            return List.of(fun);
        }
    }

    private List<FunctionalObject> checkManufacturerPartyIdentity(FunctionalObject fun, String accessToken){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/Reference_ManufacturerInfo?$skip=0&$top=2").toString();
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The manufacturer party identity response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> partyIdentityList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                partyIdentityList.add((String) map.get("ManufacturerId"));
            }
            if (!partyIdentityList.contains(fun.getPartyIdentity())) {
                fun.setLog("Invalid Party Identity");
                return List.of(fun);
            }
            return List.of();
        } catch (Exception e){
            System.out.println("The party identity API call failed");
            fun.setLog("API call failed from party identity");
            return List.of(fun);
        }
    }

    private List<FunctionalObject> checkOwnerPartyIdentity(FunctionalObject fun,String accessToken){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/Reference_OwnerInfo?$skip=0&$top=2").toString();
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The owner party identity response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> partyIdentityList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                partyIdentityList.add((String) map.get("OwnerId"));
            }
            if (!partyIdentityList.contains(fun.getPartyIdentity())) {
                fun.setLog("Invalid Party Identity");
                return List.of(fun);
            }
            return List.of();
        } catch (Exception e){
            System.out.println("The party identity API call failed");
            fun.setLog("API call failed from party identity");
            return List.of(fun);
        }
    }

    private List<FunctionalObject> checkAssetManagerIdentity(FunctionalObject fun,String accessToken){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/Reference_PersonInfoLov?$skip=0&$top=2").toString();
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The asset manager party identity response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> partyIdentityList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                partyIdentityList.add((String) map.get("PersonId"));
            }
            if (!partyIdentityList.contains(fun.getPartyIdentity())) {
                fun.setLog("Invalid Party Identity");
                return List.of(fun);
            }
            return List.of();
        } catch (Exception e){
            System.out.println("The party identity API call failed");
            fun.setLog("API call failed from party identity");
            return List.of(fun);
        }
    }

    private List<FunctionalObject> checkWorkType(List<FunctionalObject> fun,String accessToken){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/SvcschFunctionalObjectSchedulingDetailHandling.svc/Reference_WorkType?$skip=0&$top=101").toString();
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The work type response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> workTypeList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                workTypeList.add((String) map.get("WorkTypeId"));
            }
            List<FunctionalObject> invalidList = new ArrayList<>();
            for (FunctionalObject funObj : fun) {
                if (!workTypeList.contains(funObj.getWorkType())) {
                    funObj.setLog("Invalid Work Type");
                    invalidList.add(funObj);
                }
            }
            return invalidList;
        } catch (Exception e){
            System.out.println("The work type API call failed");
            List<FunctionalObject> invalidList = new ArrayList<>();
            for(FunctionalObject funObj: fun){
                funObj.setLog("API call failed from work type");
                invalidList.add(funObj);
            }
            return invalidList;
        }
    }

    private List<FunctionalObject> checkCalender(List<FunctionalObject> fun,String accessToken){
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/SvcschFunctionalObjectSchedulingDetailHandling.svc/Reference_WorkTimeCalendar?$skip=0&$top=101").toString();
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The calender response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> calenderList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                calenderList.add((String) map.get("CalendarId"));
            }
            List<FunctionalObject> invalidList = new ArrayList<>();
            for (FunctionalObject funObj : fun) {
                if (!calenderList.contains(funObj.getCalender())) {
                    funObj.setLog("Invalid Calender");
                    invalidList.add(funObj);
                }
            }
            return invalidList;
        } catch (Exception e){
            System.out.println("The calender API call failed");
            List<FunctionalObject> invalidList = new ArrayList<>();
            for(FunctionalObject funObj: fun){
                funObj.setLog("API call failed from calender");
                invalidList.add(funObj);
            }
            return invalidList;
        }
    }

    private List<FunctionalObject> checkFixedAsset(List<FunctionalObject> fun,String accessToken, Map<String,Object> defaultValues){
        String Company = defaultValues.get("Company").toString();
        System.out.println("The company is asset"+Company);
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/Reference_AccountingCodePartValue?$filter=(Company eq '").append(Company).append("' and CodePart eq 'F')").toString();
        System.out.println("The url is"+url);
        try{
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
            System.out.println("The fixed asset response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<String> fixedAssetList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                fixedAssetList.add((String) map.get("CodePartValue"));
            }
            List<FunctionalObject> invalidList = new ArrayList<>();
            for (FunctionalObject funObj : fun) {
                if (!fixedAssetList.contains(funObj.getFixedAsset())) {
                    funObj.setLog("Invalid Fixed Asset");
                    invalidList.add(funObj);
                }
            }
            return invalidList;
        } catch (Exception e){
            System.out.println("The fixed asset API call failed");
            List<FunctionalObject> invalidList = new ArrayList<>();
            for(FunctionalObject funObj: fun){
                funObj.setLog("API call failed from fixed asset");
                invalidList.add(funObj);
            }
            return invalidList;
        }
    }

    private HttpHeaders gethttpHeaders(String accessToken) {
        HttpHeaders headers= new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    @Override
    public List<FunctionalObject> findAll() {
        return List.of();
    }

    public List<FunctionalObject> getAll(List<Integer> ids, String accessToken){
        //get the EquipmentObjectSeq
        List<FunctionalObject> funList = new ArrayList<>();
        for (Integer id : ids) {
            Integer equipmentObjectSeq =retreiveData(id,accessToken);
            //get the functional object details
            FunctionalObject fun = new FunctionalObject();
            if(equipmentObjectSeq!=null) {
                HttpEntity<Void> httpEntity = new HttpEntity<>(gethttpHeaders(accessToken));
                stringBuilder.setLength(0);
                stringBuilder.append(baseURL);
                String url = stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/EquipmentFunctionalSet(EquipmentObjectSeq=").append(equipmentObjectSeq).append(")").toString();
                System.out.println("The url is" + url);
                try {
                    ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                    Map<String, Object> responseList = responseEntity.getBody();
                    fun.setObjectId((String) responseList.get("MchCode"));
                    fun.setDescription((String) responseList.get("MchName"));
                    fun.setSite((String) responseList.get("Contract"));
                    fun.setObjLevel((String) responseList.get("ObjLevel"));
                    fun.setItemClass((String) responseList.get("ItemClassId"));
                    fun.setPartNo((String) responseList.get("PartNo"));
                    fun.setInstallationDate((String) responseList.get("ProductionDate"));
                    fun.setLocationId((String) responseList.get("LocationId"));
                    fun.setBelongToObject((String) responseList.get("SupMchCode"));
                    fun.setSerialNo((String) responseList.get("SerialNo"));
                    fun.setNote((String) responseList.get("Note"));
                    fun.setFixedAsset((String) responseList.get("ObjectNo"));
                    fun.setLog("Successfull");
                    funList.add(fun);
                    System.out.println("The functional object is" + fun);

                    //get party type and party identity
//                    stringBuilder.setLength(0);
//                    stringBuilder.append(baseURL);
//                    String urlParty = stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/EquipmentFunctionalSet(EquipmentObjectSeq=").append(equipmentObjectSeq).append(")/EquipmentObjectPartyArray").toString();
//                    try {
//                        ResponseEntity<Map> responseEntityParty = restTemplate.exchange(urlParty, HttpMethod.GET, httpEntity, Map.class);
//                        System.out.println("The party type and party identity response map is" + responseEntityParty.getBody());
//                        Map<String, Object> responseMapParty = responseEntityParty.getBody();
//                        List<Map<String, Object>> responseListParty = (List<Map<String, Object>>) responseMapParty.get("value");
//                        for (Map<String, Object> map : responseListParty) {
//                            FunctionalObject funParty = fun;
//                            //add the previous funlist details
//                            funParty.setPartyType((String) map.get("PartyType"));
//                            funParty.setPartyIdentity((String) map.get("Identity"));
//                            System.out.println("The party type is" + map.get("PartyType"));
//                            System.out.println("The party identity is" + map.get("Identity"));
//                            System.out.println("The location id is" + map.get("DeliveryAddress"));
////                            funList.add(funParty);
//                        }
//
//                        //get the work type
//                        stringBuilder.setLength(0);
//                        stringBuilder.append(baseURL);
//                        String urlWorkType = stringBuilder.append("/main/ifsapplications/projection/v1/SvcschFunctionalObjectSchedulingDetailHandling.svc/FunctionalObjectDetailSet(EquipmentObjectSeq=").append(equipmentObjectSeq).append(")/ObjectAvailabilityDetailArray").toString();
//                        try {
//                            ResponseEntity<Map> responseEntityWorkType = restTemplate.exchange(urlWorkType, HttpMethod.GET, httpEntity, Map.class);
//                            System.out.println("The work type response map is" + responseEntityWorkType.getBody());
//                            Map<String, Object> responseMapWorkType = responseEntityWorkType.getBody();
//                            List<Map<String, Object>> responseListWorkType = (List<Map<String, Object>>) responseMapWorkType.get("value");
//                            for (Map<String, Object> map : responseListWorkType) {
//                                FunctionalObject funWorkType = fun;
//                                //add the previous funlist details
//                                funWorkType.setCalender((String) map.get("CalendarId"));
//                                funWorkType.setWorkType((String) map.get("WorkType"));
//                                System.out.println("The calender is" + map.get("CalendarId"));
//                                System.out.println("The work type is" + map.get("WorkType"));
////                                funList.add(funWorkType);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("The work type API call failed");
//                            fun.setLog("API call failed from work type");
//                            funList.add(fun);
//                        }
//                    } catch (Exception e) {
//                        System.out.println("The party type and party identity API call failed");
//                        fun.setLog("API call failed from party type and party identity");
//                        funList.add(fun);
//                    }

                } catch (Exception e) {
                    System.out.println("API call failed for id" + id);
                }

            }else{
                System.out.println("The equipment object seq is null");
                fun.setObjectId(String.valueOf(id));
                fun.setLog("Object Id does not exist");
                funList.add(fun);
            }
        }
        return funList;
    }

    public Integer retreiveData(int id,String accessToken) {
        HttpEntity<Void> httpEntity= new HttpEntity<>(gethttpHeaders(accessToken));
        stringBuilder.setLength(0);
        stringBuilder.append(baseURL);
        String url=stringBuilder.append("/main/ifsapplications/projection/v1/FunctionalObjectHandling.svc/EquipmentFunctionalSet?$filter=(MchCode eq '").append(id).append("')&$select=EquipmentObjectSeq,MchCode&$skip=0&$top=25").toString();
        System.out.println("The url is"+url);
        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseMap = responseEntity.getBody();
//            System.out.println("The response map is" + responseMap);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
            List<Integer> equipmentObjectSeqList = new ArrayList<>();
            for (Map<String, Object> map : responseList) {
                equipmentObjectSeqList.add((Integer) map.get("EquipmentObjectSeq"));
            }
            return equipmentObjectSeqList.get(0);
        } catch (Exception e) {
//            System.out.println("The API call failed");
            return null;
        }

    }
}
