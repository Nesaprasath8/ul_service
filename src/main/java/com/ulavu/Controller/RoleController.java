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

import com.ulavu.Entity.UL_Response;
import com.ulavu.Entity.UL_Role;
import com.ulavu.Service.RoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/role")
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/")
    public UL_Response getRoles() {
        UL_Response response = new UL_Response();
        try {
            response.status = "success";
            response.result = roleService.getRoles();
            response.message = "Role list retrieved successfully";
        } catch (Exception e) {
            log.error("Failed to retrieve roles", e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to retrieve roles";
        }
        return response;
    }

    @PostMapping("/")
    public UL_Response createRole(@Valid @RequestBody UL_Role role,
            @RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            role.lst_modifiedby = authUsername;
            String result = roleService.createRole(role);
            response.status = "success";
            response.result = result;
            response.message = "Role created successfully";
        } catch (Exception e) {
            log.error("Failed to create role '{}'", role.name, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to create role";
        }
        return response;
    }

    @PutMapping("/")
    public UL_Response updateRole(@Valid @RequestBody UL_Role role,
            @RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            role.lst_modifiedby = authUsername;
            String result = roleService.updateRole(role);
            response.status = "success";
            response.result = result;
            response.message = "Role updated successfully";
        } catch (Exception e) {
            log.error("Failed to update role '{}'", role.id, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to update role";
        }
        return response;
    }

    @DeleteMapping("/{id}")
    public UL_Response deleteRole(@PathVariable("id") int id,
            @RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            String result = roleService.deleteRole(id, authUsername);
            response.status = "success";
            response.result = result;
            response.message = "Role deleted successfully";
        } catch (Exception e) {
            log.error("Failed to delete role '{}'", id, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to delete role. It may still be assigned to users.";
        }
        return response;
    }
}
