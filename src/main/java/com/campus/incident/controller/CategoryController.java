package com.campus.incident.controller;

import com.campus.incident.entity.IncidentCategory;
import com.campus.incident.repository.IncidentCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    
    @Autowired
    private IncidentCategoryRepository categoryRepository;
    
    // Get all categories
    @GetMapping
    public ResponseEntity<List<IncidentCategory>> getAllCategories() {
        List<IncidentCategory> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }
    
    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<IncidentCategory> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Get active categories only
    @GetMapping("/active")
    public ResponseEntity<List<IncidentCategory>> getActiveCategories() {
        List<IncidentCategory> categories = categoryRepository.findByIsActive(true);
        return ResponseEntity.ok(categories);
    }
}
