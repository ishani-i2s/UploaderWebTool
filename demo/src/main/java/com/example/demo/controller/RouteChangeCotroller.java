package com.example.demo.controller;
import com.example.demo.entity.TaskDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.RouteChangeService;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RouteChangeCotroller {
    @Autowired
    RouteChangeService routeChangeService;

    @GetMapping("/getSites")
    public List<String> getSites(@RequestParam("accessToken") String accessToken) {
        System.out.println("The access token is"+accessToken);
        return routeChangeService.getSite(accessToken);
    }

    @GetMapping("/getRouteChanges")
    public ResponseEntity<Resource> getRoutes(@RequestParam("accessToken") String accessToken, @RequestParam("site") String site, @RequestParam("status") String status, @RequestParam("plannedStart") String plannedStart) {
        System.out.println("The access token is"+accessToken);
        System.out.println("The site is"+site);
        System.out.println("The status is"+status);
        System.out.println("The planned start is"+plannedStart);

        List<TaskDetails> taskDetails = routeChangeService.getTaskDetails(accessToken, site, status, plannedStart);

        ExcelHelper.taskDetailsToExcel(taskDetails);

        String filePath = "src/main/resources/static/taskDetails.xlsx";
        File file = new File(filePath);
        Path path = Paths.get(filePath);
        Resource resource = null;
        try {
            resource = new FileSystemResource(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/TaskDetailsUpdate")
    public ResponseEntity<?> updateTaskDetails(@RequestParam("file") MultipartFile file, @RequestParam("accessToken") String accessToken) {
        System.out.println("The file is"+file);
        if(ExcelHelper.hasExcelFormat(file)){
            try {
                List<TaskDetails> details=routeChangeService.updateTaskDetails(file,accessToken);
                System.out.println("The details are"+details);
                return null;
            } catch (Exception e) {
                String message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                return null;
            }
        }
        return null;
    }


}
