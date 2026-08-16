package com.ulavu.Entity;

import org.springframework.web.context.annotation.RequestScope;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RequestScope
public class UL_Role {
    public Integer id;

    @NotBlank(message = "name is required")
    @Size(max = 50, message = "name must be at most 50 characters")
    public String name;

    public String status;
    public String lst_modifiedby;
}
