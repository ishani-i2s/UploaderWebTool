package com.example.demo.controller;
import com.example.demo.entity.FixedAsset;
import com.example.demo.entity.FunctionalObject;
import com.example.demo.service.FixedAssetsService;
import com.example.demo.service.FunctionalObjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.GeneratedValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class IFSController {
    @Autowired
    FunctionalObjectService functionalObject;

    @Autowired
    FixedAssetsService fixedAssets;

    @PostMapping("/excelUpload")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file,@RequestParam("accessToken") String accessToken){
        System.out.println("Token is"+accessToken);
        if(ExcelHelper.hasExcelFormat(file)){
            try {
                List<FunctionalObject> errors=functionalObject.save(file,accessToken);
                ExcelHelper.writeToExcel(errors);
                int successCount = 0;
                for(FunctionalObject error:errors){
                   if(error.getLog()=="Successfull"){
                       successCount++;
                   }
                }
                int errorCount = errors.size()-successCount;
                Map<String,Integer> response = Map.of("successCount",successCount,"errorCount",errorCount);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                String message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
            }
        }
        return null;
    }

    @PostMapping("/excelUploadFA")
    public ResponseEntity<?> uploadExcelFA(@RequestParam("file") MultipartFile file, @RequestParam("accessToken") String accessToken) {
        System.out.println("The file is"+file);
        if(ExcelHelper.hasExcelFormat(file)){
            try {
                List<FixedAsset> errors=fixedAssets.save(file,accessToken);
                ExcelHelper.writeToExcelFA(errors);
                int successCount = 0;
                for(FixedAsset error:errors){
                    if(error.getLog()=="Successfull"){
                        successCount++;
                    }
                }
                int errorCount = errors.size()-successCount;
                Map<String,Integer> response = Map.of("successCount",successCount,"errorCount",errorCount);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                String message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
            }
        }
        return null;
    }



    @GetMapping("/excelDownload")
    public ResponseEntity<Resource> downloadExcel() {
        String filePath = "src/main/resources/static/functionalObjectErrors.xls";
        File file = new File(filePath);
        Path path = Paths.get(filePath);
        Resource resource = null;
        try {
            resource = new FileSystemResource(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/getInfo")
    public ResponseEntity<Resource> sendFODetails(@RequestParam("objArray") List<Integer> objectIds, @RequestParam("accessToken") String accessToken){
        System.out.println("The objectIds are"+objectIds);
        List<FunctionalObject> response = functionalObject.getAll(objectIds,accessToken);
        System.out.println("The response is"+response);
//
        ExcelHelper.saveToExcel(response);

        String filePath = "src/main/resources/static/functionalObjectDetail.xls";
        File file = new File(filePath);
        Path path = Paths.get(filePath);
        Resource resource = null;
        try {
            resource = new FileSystemResource(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
