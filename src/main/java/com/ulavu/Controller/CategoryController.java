package com.ulavu.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
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

    @GetMapping("/{id}")
    public UL_Response getCategoryById(@PathVariable("id") int id) {
        UL_Response response = new UL_Response();
        try {
            response.status = "success";
            response.result = categoryService.getCategoryById(id);
            response.message = "Category retrieved successfully";
        } catch (Exception e) {
            log.error("Failed to retrieve category '{}'", id, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to retrieve category";
        }
        return response;
    }

    @PostMapping("/")
    public UL_Response insertCategory(@Valid @RequestBody UL_Category category,
            @RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            category.lst_modifiedby = authUsername;
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

    @PutMapping("/")
    public UL_Response updateCategory(@Valid @RequestBody UL_Category category,
            @RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            category.lst_modifiedby = authUsername;
            String result = categoryService.updateCategory(category);
            response.status = "success";
            response.result = result;
            response.message = "Category updated successfully";
        } catch (Exception e) {
            log.error("Failed to update category '{}'", category.id, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to update category";
        }
        return response;
    }

    @DeleteMapping("/{id}")
    public UL_Response deleteCategory(@PathVariable("id") int id,
            @RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            String result = categoryService.deleteCategory(id, authUsername);
            response.status = "success";
            response.result = result;
            response.message = "Category deleted successfully";
        } catch (Exception e) {
            log.error("Failed to delete category '{}'", id, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to delete category. It may be a parent to other categories.";
        }
        return response;
    }
}
