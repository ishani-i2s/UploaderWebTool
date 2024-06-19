package com.example.demo.service.Impl;
import com.example.demo.entity.FixedAsset;
import com.example.demo.repo.FixedAssetRepo;
import com.example.demo.repo.FunctionalObjectRepo;
import com.example.demo.service.FixedAssetsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.controller.ExcelHelper;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class fixedAssetsImpl implements FixedAssetsService {
    String baseURL="https://ifscloud.tsunamit.com/";
    StringBuilder stringBuilder=new StringBuilder(baseURL);
//    String accessToken= "";

    @Autowired
    FunctionalObjectRepo functionalObject;
    @Autowired
    FixedAssetRepo fixedAssets;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<FixedAsset> save(MultipartFile file, String accessToken) {
        List<FixedAsset> errorList = new ArrayList<>();

        try{
            System.out.println("Inside the save method");
            List<FixedAsset> funList = ExcelHelper.excelToFixList(file.getInputStream());

            //get the required fields
            errorList = validateRequiredFields(funList);
            funList.removeAll(errorList);

            //get the valid objId from an api call
            errorList.addAll(validateObjId(funList,accessToken));
            funList.removeAll(errorList);

            // remove all invalid object Ids from the list
            errorList.addAll(checkObjIdExist(funList,accessToken));
            funList.removeAll(errorList);

            // remove all invalid object groups from the list
            errorList.addAll(checkObjectGroup(funList,accessToken));
            funList.removeAll(errorList);

            // remove all invalid accounts from the list
            errorList.addAll(validateAccount(funList,accessToken));
            funList.removeAll(errorList);

            // remove all invalid sites from the list
            errorList.addAll(validateSite(funList,accessToken));
            funList.removeAll(errorList);

            setValidDates(funList,accessToken);

            //save the valid functional objects to the database
//            fixedAssets.saveAll(funList);

            if(funList.size()==0){
                return errorList;
            }else{
                List<FixedAsset> postStatus= postFixedAsset(funList,accessToken);
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

    private List<FixedAsset> postFixedAsset(List<FixedAsset> funList, String accessToken) {
        System.out.println("Inside the post Fixed assets method");
        HttpHeaders headers = gethttpHeaders(accessToken);
        List<FixedAsset> invalidList = new ArrayList<>();

        for (FixedAsset fun : funList) {
            stringBuilder.setLength(0);  // Clear StringBuilder for each iteration
            String url = stringBuilder.append(baseURL).append("/main/ifsapplications/projection/v1/ObjectHandling.svc/FaObjectSet").toString();

            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("Company", fun.getCompany());
            payload.put("ObjectId", fun.getObjectId());
            payload.put("Description", fun.getDescription());
            payload.put("Account", fun.getAccount());
            payload.put("ObjectGroupId", fun.getObjectGroupId());
            payload.put("FaObjectType", fun.getFaObjectType());
            payload.put("AcquisitionReason", fun.getAcquisitionReason());
            payload.put("SiteId", fun.getSite());
            payload.put("ValidFrom", fun.getValidFrom());
            payload.put("ValidUntil", fun.getValidUntil());

            HttpEntity<Map> httpEntity = new HttpEntity<>(payload, headers);
            System.out.println("The payload is " + payload);
            System.out.println("HttpEntity is " + httpEntity);

            try {
                var response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
                if (response.getStatusCode().toString().equals("201 CREATED")) {
                    System.out.println("fixed assets created");

                    // Process books for the valid fixed asset
                    List<FixedAsset> errorListBook = processBooks(fun,accessToken);
                    if (!errorListBook.isEmpty()) {
                        invalidList.addAll(errorListBook);
                    }
                } else {
                    System.out.println("fixed asset creation failed");
                    fun.setLog("fixed asset creation failed");
                    invalidList.add(fun);
                }
            } catch (Exception e) {
                String errorResponse = e.getMessage();
                String errorMessage = extractErrorMessageFromJson(errorResponse);
                fun.setLog(errorMessage);
                invalidList.add(fun);
            }
        }
        System.out.println("The invalid list is " + invalidList);
        System.out.println("Posted all the functional objects");
        return invalidList;
    }

    private List<FixedAsset> processBooks(FixedAsset fixedAsset, String accessToken) {
        List<FixedAsset> errorListBook = new ArrayList<>();
        // Remove all invalid book Ids from the list
        errorListBook.addAll(validateBookId(Collections.singletonList(fixedAsset),accessToken));
        if (!errorListBook.isEmpty()) return errorListBook;

        // Remove all invalid depreciation methods from the list
        errorListBook.addAll(validateDepreciationMethod(Collections.singletonList(fixedAsset),accessToken));
        if (!errorListBook.isEmpty()) return errorListBook;

        // Remove all invalid Estimated Life from the list
        errorListBook.addAll(validateEstimatedLife(Collections.singletonList(fixedAsset),accessToken));
        if (!errorListBook.isEmpty()) return errorListBook;

        System.out.println("set book values");
        setBookValues(Collections.singletonList(fixedAsset),accessToken);

        return postFixedAssetBook(fixedAsset,accessToken);
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

    private List<FixedAsset> validateRequiredFields(List<FixedAsset> funList) {
        System.out.println("inside required field validations");
        List<FixedAsset> invalidList = new ArrayList<>();
        for (FixedAsset fixedAsset : funList) {
            if (fixedAsset.getObjectId() == null) {
                fixedAsset.setLog("Object Id can't be null");
                invalidList.add(fixedAsset);
            }
            else if (fixedAsset.getCompany() == null) {
                fixedAsset.setLog("company can't be null");
                invalidList.add(fixedAsset);
            }
            else if (fixedAsset.getDescription() == null) {
                System.out.println("null description");
                fixedAsset.setLog("Description can't be null");
                invalidList.add(fixedAsset);
            }
            else if (fixedAsset.getObjectGroupId() == null) {
                fixedAsset.setLog("Object Group Id can't be null");
                invalidList.add(fixedAsset);
            }
            else if (fixedAsset.getAccount() == null) {
                fixedAsset.setLog("Account can't be null");
                invalidList.add(fixedAsset);
            }
            else if (fixedAsset.getSite() == null) {
                fixedAsset.setLog("Site can't be null");
                invalidList.add(fixedAsset);
            }
            else if (fixedAsset.getAcquisitionReason() == null) {
                fixedAsset.setLog("Acquisition Reason can't be null");
                invalidList.add(fixedAsset);
            }
        }
        System.out.println("invalid list : " + invalidList);
        return invalidList;
    }

    private List<FixedAsset> validateObjId(List<FixedAsset> funList, String accessToken) {
        System.out.println("inside obj id validation");
        HttpEntity<Void> httpEntity = new HttpEntity<>(gethttpHeaders(accessToken));
        List<FixedAsset> invalidList = new ArrayList<>();
        for (FixedAsset fixedAsset : funList) {
            stringBuilder.setLength(0);
            stringBuilder.append(baseURL);
            String url = stringBuilder.append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/ValidateObjectId(ObjectId='%s')", fixedAsset.getObjectId())).toString();
            System.out.println("URL: " + url);

            try {
                System.out.println("inside try block in the validation");
                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap = responseEntity.getBody();
                System.out.println("The response map is " + responseMap);
                boolean isValid = (Boolean) responseMap.get("value");
                System.out.println("check validity : " + isValid );

                if (!isValid) {
                    System.out.println("check is not valid");
                    fixedAsset.setLog("Invalid ObjectId");
                    invalidList.add(fixedAsset);
                }
            } catch (Exception e) {
                    fixedAsset.setLog("validate obj id API call failed");
                    invalidList.add(fixedAsset);
                return invalidList;
            }
        }
        return invalidList;
    }

    private List<FixedAsset> checkObjIdExist(List<FixedAsset> funList, String accessToken) {
        System.out.println("inside object exist validation");
        HttpEntity<Void> httpEntity = new HttpEntity<>(gethttpHeaders(accessToken));
        List<FixedAsset> invalidList = new ArrayList<>();
        System.out.println("fixed asset count : " + fixedAssets.count());
        System.out.println("fixed assets : " + fixedAssets);
        System.out.println();
        for (FixedAsset fixedAsset : funList) {
            stringBuilder.setLength(0);
            stringBuilder.append(baseURL);
            String url = stringBuilder.append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/Reference_FaObject?$filter=Company eq '%s' and ObjectId eq '%s'", fixedAsset.getCompany(), fixedAsset.getObjectId())).toString();
            System.out.println("URL: " + url);
            System.out.println("check ID URL : " + url);

            try {
                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap = responseEntity.getBody();
                System.out.println("The response map is " + responseMap);
                List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");

                if (responseList != null && !responseList.isEmpty()) {
                    fixedAsset.setLog("Object Id already existing");
                    invalidList.add(fixedAsset);
                }else {
                    // If Object ID does not exist, make it uppercase
                    fixedAsset.setObjectId(fixedAsset.getObjectId().toUpperCase());
                }
            } catch (Exception e) {
//                for (FixedAsset fun : funList) {
                    fixedAsset.setLog("check object Id API call failed");
                    invalidList.add(fixedAsset);
//                }
                return invalidList;
            }
        }
        return invalidList;
    }

    private List<FixedAsset> checkObjectGroup(List<FixedAsset> funList, String accessToken) {
        System.out.println("inside validation");
        HttpEntity<Void> httpEntity = new HttpEntity<>(gethttpHeaders(accessToken));
        List<FixedAsset> invalidList = new ArrayList<>();
        for (FixedAsset fixedAsset : funList) {
            stringBuilder.setLength(0);
            stringBuilder.append(baseURL);
            String url = stringBuilder.append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/Reference_FaObjectGroup?$filter=(Company eq '%s')", fixedAsset.getCompany())).toString();
            System.out.println("URL: " + url);
            try{
                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap = responseEntity.getBody();
                System.out.println("The response map is" + responseMap);
                List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
                List<String> objGroupList = new ArrayList<>();
                for (Map<String, Object> map : responseList) {
                    objGroupList.add((String) map.get("ObjectGroupId"));
                }
                System.out.println("object group list : " + objGroupList);
//                for (FixedAsset fun : funList) {
                    if (!objGroupList.contains(fixedAsset.getObjectGroupId())) {
                        fixedAsset.setLog("Invalid Object Group");
                        invalidList.add(fixedAsset);
                    }
//                }
                return invalidList;
            } catch (Exception e){
//                for(FixedAsset fun: funList){
                    fixedAsset.setLog("check object group API call failed");
                    invalidList.add(fixedAsset);
//                }
                return invalidList;
            }
        }
        return invalidList;
    }

    public List<FixedAsset> validateAccount(List<FixedAsset> funList, String accessToken) {
        System.out.println("inside account validation");
        HttpEntity<Void> httpEntity = new HttpEntity<>(gethttpHeaders(accessToken));
        List<FixedAsset> invalidList = new ArrayList<>();
        for (FixedAsset fixedAsset : funList) {
            stringBuilder.setLength(0);
            stringBuilder.append(baseURL);
            String url = stringBuilder.append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/GetObjectGroupInfo(Company='%s',ObjectGroupId='%s')", fixedAsset.getCompany(), fixedAsset.getObjectGroupId())).toString();
            System.out.println("URL: " + url);

            try {
                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap = responseEntity.getBody();
                System.out.println("The response map is " + responseMap);

                if (responseMap != null) {
                    String responseAccount = (String) responseMap.get("Account");
                    System.out.println("Response Account: " + responseAccount);

                    if (!fixedAsset.getAccount().equals(responseAccount)) {
                        fixedAsset.setLog("Invalid Account");
                        invalidList.add(fixedAsset);
                    }
                } else {
                    fixedAsset.setLog("Invalid Response from API");
                    invalidList.add(fixedAsset);
                }
            } catch (Exception e) {
                fixedAsset.setLog("check object group API call failed");
                invalidList.add(fixedAsset);
            }
        }
        return invalidList;
    }

    private List<FixedAsset> validateSite(List<FixedAsset> funList, String accessToken) {
        System.out.println("inside site validation");
        HttpEntity<Void> httpEntity = new HttpEntity<>(gethttpHeaders(accessToken));
        List<FixedAsset> invalidList = new ArrayList<>();
        for (FixedAsset fixedAsset : funList) {
            stringBuilder.setLength(0);
            stringBuilder.append(baseURL);
            String url = stringBuilder.append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/Reference_CompanySite?$filter=(Company eq '%s')", fixedAsset.getCompany())).toString();
            System.out.println("URL: " + url);
            try{
                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap = responseEntity.getBody();
                System.out.println("The response map is" + responseMap);
                List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
                List<String> siteList = new ArrayList<>();
                for (Map<String, Object> map : responseList) {
                    siteList.add((String) map.get("Contract"));
                }
                System.out.println("site list : " + siteList);
//                for (FixedAsset fun : funList) {
                    if (!siteList.contains(fixedAsset.getSite())) {
                        fixedAsset.setLog("Invalid site Id");
                        invalidList.add(fixedAsset);
                    }
//                }
                return invalidList;
            } catch (Exception e){
//                for(FixedAsset fun: funList){
                    fixedAsset.setLog("check site API call failed");
                    invalidList.add(fixedAsset);
//                }
                return invalidList;
            }
        }
        return invalidList;
    }

    // create books

    private List<FixedAsset> validateBookId(List<FixedAsset> funList, String accessToken) {
        System.out.println("inside book Id validation");
        HttpEntity<Void> httpEntity = new HttpEntity<>(gethttpHeaders(accessToken));
        List<FixedAsset> invalidList = new ArrayList<>();

        for (FixedAsset fixedAsset : funList) {
            stringBuilder.setLength(0);
            String url = stringBuilder.append(baseURL)
                    .append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/Reference_FaBook?$filter=(Company eq '%s')", fixedAsset.getCompany()))
                    .toString();

            // Reset the StringBuilder for the next URL
            stringBuilder.setLength(0);
            String url2 = stringBuilder.append(baseURL)
                    .append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/FaObjectSet(Company='%s',ObjectId='%s')/FaBookPerObjectArray", fixedAsset.getCompany(), fixedAsset.getObjectId()))
                    .toString();

            try {
                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap = responseEntity.getBody();
                System.out.println("The response map is " + responseMap);
                List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
                List<String> bookIdList = new ArrayList<>();
                for (Map<String, Object> map : responseList) {
                    bookIdList.add((String) map.get("BookId"));
                }

                ResponseEntity<Map> responseEntity2 = restTemplate.exchange(url2, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap2 = responseEntity2.getBody();
                System.out.println("The response map is " + responseMap2);
                List<Map<String, Object>> responseList2 = (List<Map<String, Object>>) responseMap2.get("value");
                for (Map<String, Object> map : responseList2) {
                    bookIdList.remove((String) map.get("BookId"));
                }

                System.out.println("book id list : " + bookIdList);
                if (!bookIdList.contains(fixedAsset.getBookId())) {
                    fixedAsset.setLog("Invalid book Id");
                    invalidList.add(fixedAsset);
                }
            } catch (Exception e) {
                fixedAsset.setLog("book id API call failed");
                invalidList.add(fixedAsset);  // Add the fixedAsset to invalidList if an exception occurs
            }
        }
        return invalidList;
    }

    private List<FixedAsset> validateDepreciationMethod(List<FixedAsset> funList, String accessToken) {
        System.out.println("inside depreciation method validation");
        HttpEntity<Void> httpEntity = new HttpEntity<>(gethttpHeaders(accessToken));
        List<FixedAsset> invalidList = new ArrayList<>();
        for (FixedAsset fixedAsset : funList) {
            stringBuilder.setLength(0);
            stringBuilder.append(baseURL);
            String url = stringBuilder.append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/Reference_FaMethod?$filter=(Company eq '%s')", fixedAsset.getCompany())).toString();
            System.out.println("URL: " + url);
            try{
                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap = responseEntity.getBody();
                System.out.println("The response map is" + responseMap);
                List<Map<String, Object>> responseList = (List<Map<String, Object>>) responseMap.get("value");
                List<String> dmList = new ArrayList<>();
                for (Map<String, Object> map : responseList) {
                    dmList.add((String) map.get("MethodId"));
                }

                System.out.println("Method Ids : " + dmList);
                System.out.println("depreciation method :" + fixedAsset.getDepreciationMethod());
                if (!dmList.contains(fixedAsset.getDepreciationMethod())) {
                    fixedAsset.setLog("Invalid depreciation method Id");
                    invalidList.add(fixedAsset);
                }
                return invalidList;
            } catch (Exception e){
                fixedAsset.setLog("check depreciation method API call failed");
                invalidList.add(fixedAsset);
                return invalidList;
            }
        }
        return invalidList;
    }

    private List<FixedAsset> validateEstimatedLife(List<FixedAsset> funList, String accessToken) {
        System.out.println("inside estimated life validation");
        HttpEntity<Void> httpEntity = new HttpEntity<>(gethttpHeaders(accessToken));
        List<FixedAsset> invalidList = new ArrayList<>();

        for (FixedAsset fixedAsset : funList) {
//            Integer estimatedLife = fixedAsset.getEstimatedLife();

            // Check if EstimatedLife is in YYMM format
//            if (!isValidYYMMFormat(estimatedLife)) {
//                fixedAsset.setLog("Invalid format for estimated life. Expected YYMM.");
//                invalidList.add(fixedAsset);
//                continue;
//            }

            // Build URL for API call
            stringBuilder.setLength(0);
            stringBuilder.append(baseURL);
            String url = stringBuilder.append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/ValidateFormat(EstLife='%s')", fixedAsset.getEstimatedLife())).toString();
            System.out.println("URL: " + url);

            try {
                // Make API call
                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap = responseEntity.getBody();
                System.out.println("The response map is " + responseMap);

                // Check response and set the value if valid
                if (responseMap != null && responseMap.containsKey("value")) {
                    String validatedLife = (String) responseMap.get("value");
                    System.out.println("validateLife : " + validatedLife);
                    fixedAsset.setEstimatedLife(validatedLife);
                    System.out.println("Estimated life set to: " + validatedLife);
                } else {
                    fixedAsset.setLog("Invalid estimated life");
                    invalidList.add(fixedAsset);
                }
            } catch (Exception e) {
                fixedAsset.setLog("validate estimate life API call failed");
                invalidList.add(fixedAsset);
                // Continue processing the next FixedAsset instead of returning immediately
                continue;
            }
        }
        return invalidList;
    }

    private boolean isValidYYMMFormat(String estimatedLife) {
        if (estimatedLife == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\d{2}y \\d{2}m$");
        Matcher matcher = pattern.matcher(estimatedLife);
        return matcher.matches();
    }

//    private List<FixedAsset> postFixedAssetBook(List<FixedAsset> funList) {
//        System.out.println("Inside the post Fixed assets book method");
////        System.out.println("The functional object list is"+funList);
//        List<FixedAsset> invalidList= new ArrayList<>();
//        HttpHeaders headers= gethttpHeaders();
//        for (FixedAsset fixedAsset : funList){
//            stringBuilder.setLength(0);
//            stringBuilder.append(baseURL);
//            String url = stringBuilder.append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/FaObjectSet(Company='%s',ObjectId='%s')/FaBookPerObjectArray", fixedAsset.getCompany(), fixedAsset.getObjectId())).toString();
//
//            int i=0;
//            for (FixedAsset fun : funList) {
//                System.out.println("Step "+i++);
//                Map<String, Object> payload = new java.util.HashMap<>();
//                payload.put("Company", fun.getCompany());
//                payload.put("BookId", fun.getBookId());
//                payload.put("ObjectId", fun.getObjectId());
//                payload.put("MethodId", fun.getDepreciationMethod());
//                payload.put("EstimatedLifeCl", fun.getEstimatedLife());
//
////            payload.put("ValidFrom", "2024-05-14");
////            payload.put("ValidUntil", "2049-12-31");
//
//                payload.put("SalvageValue", fun.getSalvageValue());
//                System.out.println("Salvage Value : " + fun.getSalvageValue());
//                payload.put("BusinessUse", fun.getBusinessUse());
//
//                HttpEntity<Map> httpEntity = new HttpEntity<>(payload, headers);
//                System.out.println("The payload is"+payload);
//                System.out.println("HttpEntity is"+httpEntity);
//                try{
//                    var response= restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
//                    if(response.getStatusCode().toString().equals("201 CREATED")){
//                        System.out.println("success");
//                        fun.setLog("Successfull");
//                        invalidList.add(fun);
//                        continue;
//                    }else {
//                        System.out.println("post book: creation failed");
//                        fun.setLog("Book creation failed");
//                        invalidList.add(fun);
//                    }
//                }catch (Exception e) {
//                    String errorResponse = e.getMessage();
//                    String errorMessage = extractErrorMessageFromJson(errorResponse);
//                    fun.setLog(errorMessage);
//                    invalidList.add(fun);
//                }
//            }
//        }
//
//        System.out.println("The invalid list is"+invalidList);
//        System.out.println("Posted all the functional objects");
//        return invalidList;
//    }

    private List<FixedAsset> postFixedAssetBook(FixedAsset fixedAsset, String accessToken) {
        System.out.println("Inside the post Fixed assets book method");
//        System.out.println("The functional object list is"+funList);
        List<FixedAsset> invalidList= new ArrayList<>();
        HttpHeaders headers= gethttpHeaders(accessToken);
            stringBuilder.setLength(0);
            stringBuilder.append(baseURL);
            String url = stringBuilder.append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/FaObjectSet(Company='%s',ObjectId='%s')/FaBookPerObjectArray", fixedAsset.getCompany(), fixedAsset.getObjectId())).toString();
        System.out.println("URL post book : " + url);

                Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("Company", fixedAsset.getCompany());
                payload.put("BookId", fixedAsset.getBookId());
                payload.put("ObjectId", fixedAsset.getObjectId());
                payload.put("MethodId", fixedAsset.getDepreciationMethod());
                payload.put("EstimatedLifeCl", fixedAsset.getEstimatedLife());

//            payload.put("ValidFrom", "2024-05-14");
//            payload.put("ValidUntil", "2049-12-31");

                payload.put("SalvageValue", fixedAsset.getSalvageValue());
                System.out.println("Salvage Value : " + fixedAsset.getSalvageValue());
                payload.put("BusinessUse", fixedAsset.getBusinessUse());

                HttpEntity<Map> httpEntity = new HttpEntity<>(payload, headers);
                System.out.println("The payload is"+payload);
                System.out.println("HttpEntity is"+httpEntity);
                try{
                    var response= restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
                    if(response.getStatusCode().toString().equals("201 CREATED")){
                        System.out.println("success");
                        fixedAsset.setLog("Successfull");
                        invalidList.add(fixedAsset);
                    }else {
                        System.out.println("post book: creation failed");
                        fixedAsset.setLog("Book creation failed");
                        invalidList.add(fixedAsset);
                    }
                }catch (Exception e) {
                    String errorResponse = e.getMessage();
                    String errorMessage = extractErrorMessageFromJson(errorResponse);
                    fixedAsset.setLog(errorMessage);
                    invalidList.add(fixedAsset);
                }



        System.out.println("The invalid list is"+invalidList);
        System.out.println("Posted all the functional objects");
        return invalidList;
    }

    private void setBookValues(List<FixedAsset> funList, String accessToken) {
        HttpEntity<Void> httpEntity = new HttpEntity<>(gethttpHeaders(accessToken));
        List<FixedAsset> invalidList = new ArrayList<>();

        for (FixedAsset fixedAsset : funList) {
            StringBuilder stringBuilder = new StringBuilder();  // Initialize stringBuilder for each iteration
            stringBuilder.append(baseURL);

            String url = stringBuilder.append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/FaObjectSet(Company='%s',ObjectId='%s')/FaBookPerObjectArray/IfsApp.ObjectHandling.FaBookPerObject_Default()", fixedAsset.getCompany(),fixedAsset.getObjectId())).toString();
            System.out.println("URL: " + url);

            try {
                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap = responseEntity.getBody();
                System.out.println("The response map is " + responseMap);

                if (responseMap != null) {
                    Integer SalvageValue = (Integer) responseMap.get("SalvageValue");
                    Integer BusinessUse = (Integer) responseMap.get("BusinessUse");
                    fixedAsset.setSalvageValue(SalvageValue);
                    fixedAsset.setBusinessUse(BusinessUse);

                    if (SalvageValue == null || BusinessUse == null) {
                        fixedAsset.setLog("Invalid default values");
                        invalidList.add(fixedAsset);
                    }
                } else {
                    fixedAsset.setLog("Response map is null");
                    invalidList.add(fixedAsset);
                }

            } catch (Exception e) {
                fixedAsset.setLog("set valid dates API call failed");
                invalidList.add(fixedAsset);
            }
        }
        // Process invalidList if needed
        if (!invalidList.isEmpty()) {
            System.out.println("Invalid assets: " + invalidList);
        }
    }

    private void setValidDates(List<FixedAsset> funList, String accessToken) {
        System.out.println("inside set valid dates");
        System.out.println("valid list : " + funList + " list count : " + funList.size());
        HttpEntity<Void> httpEntity = new HttpEntity<>(gethttpHeaders(accessToken));
        List<FixedAsset> invalidList = new ArrayList<>();

        for (FixedAsset fixedAsset : funList) {
            StringBuilder stringBuilder = new StringBuilder();  // Initialize stringBuilder for each iteration
            stringBuilder.append(baseURL);
            System.out.println("----------------------------------");
            System.out.println("get company :" + fixedAsset.getCompany() + " get account : " + fixedAsset.getAccount());

            String url = stringBuilder.append(String.format("/main/ifsapplications/projection/v1/ObjectHandling.svc/FaObjectSet/IfsApp.ObjectHandling.FaObject_Default(Company='%s')", fixedAsset.getCompany())).toString();
            System.out.println("URL: " + url);

            try {
                System.out.println("inside try block in set dates");
                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
                Map<String, Object> responseMap = responseEntity.getBody();
                System.out.println("The response map is " + responseMap);

                if (responseMap != null) {
                    String validFrom = (String) responseMap.get("ValidFrom");
                    String validUntil = (String) responseMap.get("ValidUntil");
                    fixedAsset.setValidFrom(validFrom);
                    fixedAsset.setValidUntil(validUntil);

                    if (validFrom == null || validUntil == null) {
                        fixedAsset.setLog("Invalid valid dates");
                        invalidList.add(fixedAsset);
                    }
                } else {
                    fixedAsset.setLog("Response map is null");
                    invalidList.add(fixedAsset);
                }

            } catch (Exception e) {
                fixedAsset.setLog("set valid dates API call failed");
                invalidList.add(fixedAsset);
            }
        }
        // Process invalidList if needed
        if (!invalidList.isEmpty()) {
            System.out.println("Invalid assets: " + invalidList);
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
    public List<FixedAsset> findAll() {
        return List.of();
    }
}
