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

import com.ulavu.Entity.UL_Address;
import com.ulavu.Entity.UL_Response;
import com.ulavu.Service.AddressService;

import jakarta.validation.Valid;

/**
 * All operations here are scoped to the caller's own addresses.
 * `authUsername` comes from JwtFilter (never from the request body or a
 * path parameter), so there is no way for a caller to read, edit, or
 * delete another user's address through this API.
 */
@RestController
@RequestMapping("/api/v1/address")
public class AddressController {

    private static final Logger log = LoggerFactory.getLogger(AddressController.class);

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/")
    public UL_Response getAddresses(@RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            response.status = "success";
            response.result = addressService.getAddresses(authUsername);
            response.message = "Address list retrieved successfully";
        } catch (Exception e) {
            log.error("Failed to retrieve addresses for '{}'", authUsername, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to retrieve addresses";
        }
        return response;
    }

    @PostMapping("/")
    public UL_Response createAddress(@Valid @RequestBody UL_Address address,
            @RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            String result = addressService.createAddress(authUsername, address);
            response.status = "success";
            response.result = result;
            response.message = "Address created successfully";
        } catch (Exception e) {
            log.error("Failed to create address for '{}'", authUsername, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to create address";
        }
        return response;
    }

    @PutMapping("/")
    public UL_Response updateAddress(@Valid @RequestBody UL_Address address,
            @RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            String result = addressService.updateAddress(authUsername, address);
            response.status = "success";
            response.result = result;
            response.message = "Address updated successfully";
        } catch (Exception e) {
            log.error("Failed to update address '{}' for '{}'", address.id, authUsername, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to update address";
        }
        return response;
    }

    @DeleteMapping("/{id}")
    public UL_Response deleteAddress(@PathVariable("id") int id,
            @RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            String result = addressService.deleteAddress(authUsername, id);
            response.status = "success";
            response.result = result;
            response.message = "Address deleted successfully";
        } catch (Exception e) {
            log.error("Failed to delete address '{}' for '{}'", id, authUsername, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to delete address";
        }
        return response;
    }
}
