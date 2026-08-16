package com.ulavu.Entity;

import org.springframework.web.context.annotation.RequestScope;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@RequestScope
public class UL_Product {
    public Integer id;

    @NotBlank(message = "name is required")
    @Size(max = 150, message = "name must be at most 150 characters")
    public String name;

    public String description;

    public String price;

    public String category;

    @Size(max = 150, message = "slug must be at most 150 characters")
    public String slug;

    public String comparePrice;

    @PositiveOrZero(message = "quantity must not be negative")
    public Integer quantity;

    public Character status;
    public String lst_modifiedby;
}
