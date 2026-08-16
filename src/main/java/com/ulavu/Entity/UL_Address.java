package com.ulavu.Entity;

import org.springframework.web.context.annotation.RequestScope;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RequestScope
public class UL_Address {
    // No userId field here on purpose: ownership is always resolved
    // server-side from the authenticated caller's JWT (see
    // AddressController), never trusted from the request body.
    public Integer id;

    @NotBlank(message = "addressLine1 is required")
    @Size(max = 200)
    public String addressLine1;

    @Size(max = 200)
    public String addressLine2;

    @NotBlank(message = "city is required")
    @Size(max = 75)
    public String city;

    @NotBlank(message = "state is required")
    @Size(max = 75)
    public String state;

    @NotBlank(message = "postalcode is required")
    @Size(max = 20)
    public String postalcode;

    @NotBlank(message = "country is required")
    @Size(max = 50)
    public String country;

    @Size(max = 20)
    public String addType;

    @Pattern(regexp = "^[yn]$", message = "isDefault must be 'y' or 'n'")
    public String isDefault;

    public String lst_modifiedby;
}
