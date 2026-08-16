package com.ulavu.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ulavu.Entity.UL_Response;
import com.ulavu.Entity.UL_UserEntity;
import com.ulavu.Service.UserService;

/**
 * Self-service profile endpoints, separate from the public /user/login and
 * /user/create endpoints. Everything here sits under /api/v1/user, so
 * JwtFilter enforces a valid token on all of it.
 *
 * The identity acted on is always the token's own username
 * (`authUsername`, set by JwtFilter) - a caller can only ever read,
 * update, or delete their OWN account this way. There is deliberately no
 * "update/delete by username" endpoint that takes an arbitrary target,
 * since this app has no role-based authorization yet to safely restrict
 * that to admins (see task summary for details).
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserProfileController {

    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    private final UserService service;

    public UserProfileController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public UL_Response getMyProfile(@RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            UL_UserEntity profile = service.getUserProfile(authUsername);
            if (profile == null) {
                response.status = "error";
                response.result = null;
                response.message = "Profile not found";
                return response;
            }
            response.status = "success";
            response.result = profile;
            response.message = "Profile retrieved successfully";
        } catch (Exception e) {
            log.error("Failed to retrieve profile for '{}'", authUsername, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to retrieve profile";
        }
        return response;
    }

    @PutMapping("/me")
    public UL_Response updateMyProfile(@RequestBody UL_UserEntity userdetail,
            @RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();

        // This is a partial-update endpoint (the stored procedure COALESCEs
        // any null field onto the existing value), so unlike login/create,
        // fields other than the ones actually being changed are optional.
        // Still validate what IS provided rather than skipping validation
        // entirely.
        if (userdetail.password != null && !userdetail.password.isBlank()
                && (userdetail.password.length() < 8 || userdetail.password.length() > 75)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "password must be between 8 and 75 characters");
        }
        if (userdetail.emailId != null && !userdetail.emailId.isBlank()
                && !userdetail.emailId.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emailId must be a valid email address");
        }

        try {
            // Identity and role are never taken from the request body:
            // - username is fixed to the token's own identity, so this can
            //   never be used to edit a different account.
            // - userrole is intentionally left untouched here (null), so a
            //   user can never grant themselves a different role through
            //   their own profile update.
            userdetail.username = authUsername;
            userdetail.userrole = null;
            userdetail.lst_modifiedby = authUsername;

            String result = service.updateUser(userdetail);
            response.status = "success";
            response.result = result;
            response.message = "Profile updated successfully";
        } catch (Exception e) {
            log.error("Failed to update profile for '{}'", authUsername, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to update profile";
        }
        return response;
    }

    @DeleteMapping("/me")
    public UL_Response deleteMyAccount(@RequestAttribute("authUsername") String authUsername) {
        UL_Response response = new UL_Response();
        try {
            UL_UserEntity userdetail = new UL_UserEntity();
            userdetail.username = authUsername;
            userdetail.lst_modifiedby = authUsername;

            String result = service.deleteUser(userdetail);
            response.status = "success";
            response.result = result;
            response.message = "Account deleted successfully";
        } catch (Exception e) {
            log.error("Failed to delete account for '{}'", authUsername, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to delete account";
        }
        return response;
    }
}
