package com.ulavu.Entity;

import org.springframework.web.context.annotation.RequestScope;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@RequestScope
public class UL_Product {
    public Integer id;

    @NotBlank(message = "name is required")
    @Size(max = 150, message = "name must be at most 150 characters")
    public String name;

    public String description;

    @NotBlank(message = "price is required")
    @Size(max = 20, message = "price must be at most 20 characters")
    public String price;

    @NotBlank(message = "comparePrice is required")
    @Size(max = 20, message = "comparePrice must be at most 20 characters")
    public String comparePrice;

    // FK to ul_category.cat_id - required on create/update. The client
    // must send the id, not a name: ul_product.prd_category_id is an
    // integer FK, there is no free-text category column on the table.
    @NotNull(message = "categoryId is required")
    public Integer categoryId;

    // Read-only: populated from the ul_category join when a product is
    // fetched. Ignored on create/update - setting this has no effect,
    // only categoryId does.
    public String categoryName;

    @Size(max = 150, message = "slug must be at most 150 characters")
    public String slug;

    @PositiveOrZero(message = "quantity must not be negative")
    public Integer quantity;

    public Character status;
    public String lst_modifiedby;
}
