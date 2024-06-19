package com.example.demo.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import jakarta.persistence.Id;

@Data
@Entity
public class FunctionalObject {
    @Id
    String ObjectId;
    String Description;
    String Site;
    String ObjLevel;
    String ItemClass;
    String PartNo;
    String LocationId;
    String BelongToObject;
    String PartyType;
    String PartyIdentity;
    String WorkType;
    String Calender;
    String Note;
    String SerialNo;
    String InstallationDate;
    String FixedAsset;
    String log;
}
