package com.ulavu.Entity;

import org.springframework.web.context.annotation.RequestScope;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RequestScope
public class UL_Category {
    public Integer id;

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    public String name;

    public String description;

    @Size(max = 150, message = "slug must be at most 150 characters")
    public String slug;

    public Integer par_id;
    public String lst_modifiedby;
}
