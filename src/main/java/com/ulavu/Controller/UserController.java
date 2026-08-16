package com.ulavu.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ulavu.Entity.UL_Response;
import com.ulavu.Entity.UL_UserEntity;
import com.ulavu.Security.JwtUtil;
import com.ulavu.Security.LoginAttemptService;
import com.ulavu.Service.UserService;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService service;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;

    @Autowired
    public UserController(UserService service, JwtUtil jwtUtil, LoginAttemptService loginAttemptService) {
        this.service = service;
        this.jwtUtil = jwtUtil;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/login")
    public UL_Response login(@Valid @RequestBody UL_UserEntity userdetail) {
        UL_Response response = new UL_Response();

        String attemptKey = userdetail.username == null ? "" : userdetail.username.trim().toLowerCase();

        if (loginAttemptService.isBlocked(attemptKey)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed login attempts. Please try again later.");
        }

        try {
            UL_UserEntity user = this.service.checkLogin(userdetail);
            if (user == null) {
                loginAttemptService.recordFailure(attemptKey);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }

            loginAttemptService.recordSuccess(attemptKey);

            String token = jwtUtil.generateToken(user.username, user.userrole, user.emailId);
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("username", user.username);
            result.put("emailId", user.emailId);
            result.put("roleId", user.userrole);

            response.status = "success";
            response.result = result;
            response.message = "Login successful";
        } catch (ResponseStatusException rse) {
            throw rse; // let framework return the intended status (401 / 429)
        } catch (Exception e) {
            loginAttemptService.recordFailure(attemptKey);
            log.error("Login failed for user '{}'", attemptKey, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
        return response;
    }

    // Profile fetch/update/delete now live in UserProfileController under
    // /api/v1/user/me, protected by JwtFilter. This old stub referenced a
    // service method (editUser) that never existed, and login/create must
    // stay public here so a client can obtain a token in the first place.

    @PostMapping("/create")
    public UL_Response createUser(@Valid @RequestBody UL_UserEntity userdetail) {
        UL_Response response = new UL_Response();
        try {
            // SECURITY: this is a public self-registration endpoint. The client must
            // never be allowed to choose its own role or forge the audit "last
            // modified by" actor - both were previously taken as-is from the request
            // body, which let any caller register themselves with an arbitrary
            // userrole (privilege escalation) and spoof the audit trail.
            // Role defaults to 'user' inside ul_sp_usermanaging when null.
            userdetail.userrole = null;
            userdetail.lst_modifiedby = userdetail.username;

            String result = this.service.createUser(userdetail);
            if (result.equalsIgnoreCase("Success")) {
                response.status = "success";
                response.result = result;
                response.message = "User created successfully";
            } else {
                response.status = "error";
                response.result = result;
                response.message = "Failed to create user";
            }
        } catch (Exception e) {
            log.error("Failed to create user '{}'", userdetail.username, e);
            response.status = "error";
            response.result = null;
            response.message = "Failed to create user";
        }
        return response;
    }

}
