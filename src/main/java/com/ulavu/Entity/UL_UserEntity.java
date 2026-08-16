package com.ulavu.Entity;

import org.springframework.web.context.annotation.RequestScope;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RequestScope
public class UL_UserEntity {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
    public String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 75, message = "password must be between 8 and 75 characters")
    public String password;

    public Integer userId;

    @Email(message = "emailId must be a valid email address")
    public String emailId;

    @NotBlank(message = "firstname is required")
    public String firstname;

    @NotBlank(message = "lastname is required")
    public String lastname;

    @Pattern(regexp = "^$|^[0-9]{7,15}$", message = "mobileno must contain 7 to 15 digits")
    public String mobileno;

    // Intentionally NOT server-trusted on create: controllers must null this
    // out before calling the service for public/self-registration endpoints.
    public Integer userrole;

    public String status;
    public String lst_modifiedby;
}
