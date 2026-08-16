package com.ulavu.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ulavu.Entity.UL_Product;
import com.ulavu.Entity.UL_Response;
import com.ulavu.Service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public UL_Response getProducts() {
        UL_Response response = new UL_Response();
        try {
            response.status = "success";
            response.result = productService.getProducts(new com.ulavu.Entity.UL_Product(), "all");
            response.message = "Product list retrieved successfully";
        } catch (Exception e) {
            log.error("Failed to retrieve products", e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to retrieve products";
        }
        return response;
    }

    @GetMapping("/{id}")
    public UL_Response getProductById(@PathVariable("id") int id) {
        UL_Response response = new UL_Response();
        try {
            response.status = "success";
            response.result = productService.getProductById(id);
            response.message = "Product retrieved successfully";
        } catch (Exception e) {
            log.error("Failed to retrieve product '{}'", id, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to retrieve product";
        }
        return response;
    }

    @PostMapping("/")
    public UL_Response createProduct(@Valid @RequestBody UL_Product product) {
        UL_Response response = new UL_Response();
        try {
            String result = productService.createProduct(product);
            if ("Success".equalsIgnoreCase(result)) {
                response.status = "success";
                response.result = result;
                response.message = "Product created successfully";
            } else {
                response.status = "error";
                response.result = result;
                response.message = "Failed to create product";
            }
        } catch (Exception e) {
            log.error("Failed to create product '{}'", product.name, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to create product";
        }
        return response;
    }

}
