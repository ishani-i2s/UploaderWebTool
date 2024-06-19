package com.example.demo.repo;
import com.example.demo.entity.FunctionalObject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedAssetRepo extends JpaRepository<FunctionalObject, Integer> {
}
