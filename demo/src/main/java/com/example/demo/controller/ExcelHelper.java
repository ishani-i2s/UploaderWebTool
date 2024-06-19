package com.example.demo.controller;

import com.example.demo.entity.FixedAsset;
import com.example.demo.entity.FunctionalObject;
import com.example.demo.entity.Student;
import com.example.demo.entity.TaskDetails;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hibernate.type.descriptor.java.JdbcDateJavaType.DATE_FORMAT;

public class ExcelHelper {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static boolean hasExcelFormat(MultipartFile file) {
        if (!"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(file.getContentType())) {
            return false;
        }
        return true;
    }

    public static List<Student> excelToStuList(InputStream inputStream) throws IOException {
        try{
            List<Student> students = new ArrayList<>();
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
//            System.out.println("The sheet is"+sheet);
//            System.out.println("The sheet rows"+sheet.getPhysicalNumberOfRows());

            for(int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                int id = (int) row.getCell(0).getNumericCellValue();
                String studentName = row.getCell(1).getStringCellValue();
                String email = row.getCell(2).getStringCellValue();
                int phone = (int) row.getCell(3).getNumericCellValue();
                String phone1 = String.valueOf(phone);
//                System.out.println("The id is"+id);
//                System.out.println("The student name is"+studentName);
//                System.out.println("The email is"+email);
//                System.out.println("The phone is"+phone1);
                Student student = new Student();
                student.setId(id);
                student.setStudentName(studentName);
                student.setEmail(email);
                student.setPhone(phone1);
                students.add(student);
            }
            workbook.close();
            System.out.println("The student list is"+students);
            return students;
        }catch (Exception e){
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public static String getCellValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                return dateFormat.format(date);
            } else {
                return String.valueOf(cell.getNumericCellValue());
            }
        } else {
            return null;
        }
    }

    public static List<FunctionalObject> excelToFunList(InputStream inputStream) {
        try{
            List<FunctionalObject> funList = new ArrayList<>();
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            System.out.println("The sheet is"+sheet);
            System.out.println("The sheet rows"+sheet.getPhysicalNumberOfRows());

            int rowNo=0;
            int rowCountWithData = 0;
            for (Row row : sheet) {
                boolean rowHasData = false;
                // Iterate through each cell in the row
                for (Cell cell : row) {
                    // Check if cell is not empty
                    if (cell.getCellType() != CellType.BLANK) {
                        rowHasData = true;
                        break;
                    }
                }
                // If the row has at least one non-empty cell, consider it as a row with data
                if (rowHasData) {
                    rowCountWithData++;
                }
            }
            System.out.println("Number of rows with data: " + rowCountWithData);

            for(int i = 2; i < rowCountWithData; i++) {
                Row row = sheet.getRow(i);
                FunctionalObject functionalObject = new FunctionalObject();
                functionalObject.setObjectId(getCellValue(row.getCell(1)));
                functionalObject.setDescription(getCellValue(row.getCell(2)));
                functionalObject.setSite(getCellValue(row.getCell(3)));
                functionalObject.setObjLevel(getCellValue(row.getCell(4)));
                functionalObject.setItemClass(getCellValue(row.getCell(5)));
                functionalObject.setPartNo(getCellValue(row.getCell(6)));
                functionalObject.setInstallationDate(getCellValue(row.getCell(7)));
                functionalObject.setLocationId(getCellValue(row.getCell(8)));
                functionalObject.setBelongToObject(getCellValue(row.getCell(9)));
                functionalObject.setSerialNo(getCellValue(row.getCell(10)));
                functionalObject.setNote(getCellValue(row.getCell(12)));
                functionalObject.setFixedAsset(getCellValue(row.getCell(11)));
                functionalObject.setPartyType(getCellValue(row.getCell(13)));
                functionalObject.setPartyIdentity(getCellValue(row.getCell(14)));
                functionalObject.setWorkType(getCellValue(row.getCell(15)));
                functionalObject.setCalender(getCellValue(row.getCell(16)));
                System.out.println("The functional object is"+functionalObject);
                funList.add(functionalObject);
            }
            System.out.println("The Functional Object List is"+funList);
            workbook.close();
            return funList;
        }catch (Exception e){
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public static void writeToExcel(List<FunctionalObject> errors) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Log");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ObjectId");
        header.createCell(1).setCellValue("Description");
        header.createCell(2).setCellValue("Site");
        header.createCell(3).setCellValue("ObjLevel");
        header.createCell(4).setCellValue("Item Class");
        header.createCell(5).setCellValue("Part No");
        header.createCell(6).setCellValue("LocationId");
        header.createCell(7).setCellValue("BelongToObject");
        header.createCell(8).setCellValue("PartyType");
        header.createCell(9).setCellValue("PartyIdentity");
        header.createCell(10).setCellValue("WorkType");
        header.createCell(11).setCellValue("Calender");
        header.createCell(12).setCellValue("InstallationDate");
        header.createCell(13).setCellValue("Note");
        header.createCell(14).setCellValue("SerialNo");
        header.createCell(15).setCellValue("Fixed Asset");
        header.createCell(16).setCellValue("Log");

        int rowNum = 1;
        for (FunctionalObject error : errors) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(error.getObjectId());
            row.createCell(1).setCellValue(error.getDescription());
            row.createCell(2).setCellValue(error.getSite());
            row.createCell(3).setCellValue(error.getObjLevel());
            row.createCell(4).setCellValue(error.getItemClass());
            row.createCell(5).setCellValue(error.getPartNo());
            row.createCell(6).setCellValue(error.getLocationId());
            row.createCell(7).setCellValue(error.getBelongToObject());
            row.createCell(8).setCellValue(error.getPartyType());
            row.createCell(9).setCellValue(error.getPartyIdentity());
            row.createCell(10).setCellValue(error.getWorkType());
            row.createCell(11).setCellValue(error.getCalender());
            row.createCell(12).setCellValue(error.getInstallationDate());
            row.createCell(13).setCellValue(error.getNote());
            row.createCell(14).setCellValue(error.getSerialNo());
            row.createCell(15).setCellValue(error.getLog());
        }

        String filePath = "src/main/resources/static/functionalObjectErrors.xls"; // Adjust the path as needed
        Path outputPath = Paths.get(filePath);

        try {
            workbook.write(new FileOutputStream(outputPath.toFile()));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FixedAsset> excelToFixList(InputStream inputStream) {
        try{
            List<FixedAsset> funList = new ArrayList<>();
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            System.out.println("The sheet is"+sheet);
            System.out.println("The sheet rows"+sheet.getPhysicalNumberOfRows());

            int rowNo=0;
            int rowCountWithData = 0;
            for (Row row : sheet) {
                boolean rowHasData = false;
                // Iterate through each cell in the row
                for (Cell cell : row) {
                    // Check if cell is not empty
                    if (cell.getCellType() != CellType.BLANK) {
                        rowHasData = true;
                        break;
                    }
                }
                // If the row has at least one non-empty cell, consider it as a row with data
                if (rowHasData) {
                    rowCountWithData++;
                }
            }
            System.out.println("Number of rows with data: " + rowCountWithData);

            for(int i = 2; i < rowCountWithData; i++) {
                System.out.println("ok");
                Row row = sheet.getRow(i);
                FixedAsset fixedAsset = new FixedAsset();
                fixedAsset.setCompany(row.getCell(1).getStringCellValue());
                fixedAsset.setObjectId(row.getCell(2).getStringCellValue());
                fixedAsset.setDescription(row.getCell(3).getStringCellValue());
                fixedAsset.setFaObjectType(row.getCell(4).getStringCellValue());
                fixedAsset.setObjectGroupId(row.getCell(5).getStringCellValue());
                fixedAsset.setAcquisitionReason(row.getCell(6).getStringCellValue());
                fixedAsset.setSite(row.getCell(7).getStringCellValue());
                fixedAsset.setBookId(row.getCell(8).getStringCellValue());
                fixedAsset.setDepreciationMethod(row.getCell(9).getStringCellValue());
                int estimatedLifeValue = (int) row.getCell(10).getNumericCellValue();
                fixedAsset.setEstimatedLife(Integer.toString(estimatedLifeValue));
//                fixedAsset.setEstimatedLife((int)row.getCell(10).getNumericCellValue());
                fixedAsset.setAccount(row.getCell(11).getStringCellValue());
                funList.add(fixedAsset);
            }
            System.out.println("The Functional Object List is"+funList);
            workbook.close();
            return funList;
        }catch (Exception e){
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }


    public static void writeToExcelFA(List<FixedAsset> errors) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Log");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Company");
        header.createCell(1).setCellValue("Object");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Acquisition Reason");
        header.createCell(4).setCellValue("Object Type");
        header.createCell(5).setCellValue("Object Group");
        header.createCell(6).setCellValue("Account");
        header.createCell(7).setCellValue("Site");
        header.createCell(8).setCellValue("Book ID");
        header.createCell(9).setCellValue("Depreciation Method");
        header.createCell(10).setCellValue("Estimated Life");
        header.createCell(11).setCellValue("Log");

        int rowNum = 1;
        for (FixedAsset error : errors) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(error.getCompany());
            row.createCell(1).setCellValue(error.getObjectId());
            row.createCell(2).setCellValue(error.getDescription());
            row.createCell(3).setCellValue(error.getAcquisitionReason());
            row.createCell(4).setCellValue(error.getFaObjectType());
            row.createCell(5).setCellValue(error.getObjectGroupId());
            row.createCell(6).setCellValue(error.getAccount());
            row.createCell(7).setCellValue(error.getSite());
            row.createCell(8).setCellValue(error.getBookId());
            row.createCell(9).setCellValue(error.getDepreciationMethod());
            row.createCell(10).setCellValue(error.getEstimatedLife());
            row.createCell(11).setCellValue(error.getLog());
        }

        String filePath = "src/main/resources/static/functionalObjectErrors.xls"; // Adjust the path as needed
        Path outputPath = Paths.get(filePath);

        try {
            workbook.write(new FileOutputStream(outputPath.toFile()));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveToExcel(List<FunctionalObject> errors){
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Log");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ObjectId");
        header.createCell(1).setCellValue("Description");
        header.createCell(2).setCellValue("Site");
        header.createCell(3).setCellValue("ObjLevel");
        header.createCell(4).setCellValue("Item Class");
        header.createCell(5).setCellValue("Part No");
        header.createCell(6).setCellValue("LocationId");
        header.createCell(7).setCellValue("BelongToObject");
        header.createCell(8).setCellValue("PartyType");
        header.createCell(9).setCellValue("PartyIdentity");
        header.createCell(10).setCellValue("WorkType");
        header.createCell(11).setCellValue("Calender");
        header.createCell(12).setCellValue("InstallationDate");
        header.createCell(13).setCellValue("Note");
        header.createCell(14).setCellValue("SerialNo");
        header.createCell(15).setCellValue("Fixed Asset");
        header.createCell(16).setCellValue("Log");

        int rowNum = 1;
        for (FunctionalObject error : errors) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(error.getObjectId());
            row.createCell(1).setCellValue(error.getDescription());
            row.createCell(2).setCellValue(error.getSite());
            row.createCell(3).setCellValue(error.getObjLevel());
            row.createCell(4).setCellValue(error.getItemClass());
            row.createCell(5).setCellValue(error.getPartNo());
            row.createCell(6).setCellValue(error.getLocationId());
            row.createCell(7).setCellValue(error.getBelongToObject());
            row.createCell(8).setCellValue(error.getPartyType());
            row.createCell(9).setCellValue(error.getPartyIdentity());
            row.createCell(10).setCellValue(error.getWorkType());
            row.createCell(11).setCellValue(error.getCalender());
            row.createCell(12).setCellValue(error.getInstallationDate());
            row.createCell(13).setCellValue(error.getNote());
            row.createCell(14).setCellValue(error.getSerialNo());
            row.createCell(15).setCellValue(error.getFixedAsset());
            row.createCell(16).setCellValue(error.getLog());
        }

        String filePath = "src/main/resources/static/functionalObjectDetail.xls"; // Adjust the path as needed
        Path outputPath = Paths.get(filePath);

        try {
            workbook.write(new FileOutputStream(outputPath.toFile()));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void taskDetailsToExcel(List<TaskDetails> details){
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Task Details");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("TaskId");
        header.createCell(1).setCellValue("Description");
        header.createCell(2).setCellValue("Site");
        header.createCell(3).setCellValue("Status");
        header.createCell(4).setCellValue("PlannedStartDate");
        header.createCell(5).setCellValue("WorkType");
        header.createCell(6).setCellValue("ObjectId");

        int rowNum = 1;
        for (TaskDetails detail : details) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(detail.getTaskId());
            row.createCell(1).setCellValue(detail.getDescription());
            row.createCell(2).setCellValue(detail.getSite());
            row.createCell(3).setCellValue(detail.getStatus());
            row.createCell(4).setCellValue(detail.getPlannedStartDate());
            row.createCell(5).setCellValue(detail.getWorkType());
            row.createCell(6).setCellValue(detail.getObjectId());
        }

        String filePath = "src/main/resources/static/taskDetails.xlsx"; // Adjust the path as needed
        Path outputPath = Paths.get(filePath);

        try {
            workbook.write(new FileOutputStream(outputPath.toFile()));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<TaskDetails> readTaskDetails (InputStream inputStream){
        try{
            List<TaskDetails> taskDetailsList = new ArrayList<>();
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            System.out.println("The sheet is"+sheet);
            System.out.println("The sheet rows"+sheet.getPhysicalNumberOfRows());

            for(int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                TaskDetails taskDetails = new TaskDetails();
                taskDetails.setTaskId(getCellValue(row.getCell(0)));
                taskDetails.setDescription(getCellValue(row.getCell(1)));
                taskDetails.setSite(getCellValue(row.getCell(2)));
                taskDetails.setStatus(getCellValue(row.getCell(3)));
                taskDetails.setPlannedStartDate(getCellValue(row.getCell(4)));
                taskDetails.setWorkType(getCellValue(row.getCell(5)));
                taskDetails.setObjectId(getCellValue(row.getCell(6)));
                taskDetailsList.add(taskDetails);
            }
            System.out.println("The task details list is"+taskDetailsList);
            workbook.close();
            return taskDetailsList;
        }catch (Exception e){
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

}
