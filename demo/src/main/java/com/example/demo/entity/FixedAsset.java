package com.example.demo.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import jakarta.persistence.Id;

@Data
@Entity
public class FixedAsset {
    @Id
    String Company;
    String ObjectId;
    String Description;
    String ObjectGroupId;
    String AcquisitionReason;
    String ValidFrom;
    String ValidUntil;
    String Account;
    String FaObjectType;
    String Site;
    String BookId;
    String DepreciationMethod;
    String EstimatedLife;
    Integer SalvageValue;
    Integer BusinessUse;
    String log;
}