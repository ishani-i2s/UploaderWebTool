package com.example.demo.entity;

import lombok.Data;

@Data
public class TaskDetails {
    String TaskId;
    String Description;
    String Site;
    String Status;
    String PlannedStartDate;
    String WorkType;
    String ObjectId;
}
