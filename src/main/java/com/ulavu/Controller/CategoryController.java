package com.ulavu.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ulavu.Entity.UL_Category;
import com.ulavu.Entity.UL_Response;
import com.ulavu.Service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public UL_Response getCategories() {
        UL_Response response = new UL_Response();
        try {
            response.status = "success";
            response.result = categoryService.getCategories();
            response.message = "Category list retrieved successfully";
        } catch (Exception e) {
            log.error("Failed to retrieve categories", e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to retrieve categories";
        }
        return response;
    }

    @PostMapping("/")
    public UL_Response insertCategory(@Valid @RequestBody UL_Category category) {
        UL_Response response = new UL_Response();
        try {
            String result = categoryService.insertCategory(category);
            if ("Success".equalsIgnoreCase(result)) {
                response.status = "success";
                response.result = result;
                response.message = "Category created successfully";
            } else {
                response.status = "error";
                response.result = result;
                response.message = "Failed to create category";
            }
        } catch (Exception e) {
            log.error("Failed to create category '{}'", category.name, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to create category";
        }
        return response;
    }
}
