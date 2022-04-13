/*
 * Copyright 2021-2021 Monkey Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.monkey.mmq.web.controller;


import com.fasterxml.jackson.databind.node.ObjectNode;
import org.monkey.mmq.auth.common.AuthConfigs;
import org.monkey.mmq.auth.common.AuthSystemTypes;
import org.monkey.mmq.auth.exception.AccessException;
import org.monkey.mmq.auth.model.User;
import org.monkey.mmq.auth.service.IMqttAuthService;
import org.monkey.mmq.core.common.Constants;
import org.monkey.mmq.core.consistency.model.RestResult;
import org.monkey.mmq.core.consistency.model.RestResultUtils;
import org.monkey.mmq.core.utils.JacksonUtils;
import org.monkey.mmq.core.utils.PasswordEncoderUtil;
import org.monkey.mmq.web.security.JwtTokenManager;
import org.monkey.mmq.web.security.MmqAuthConfig;
import org.monkey.mmq.web.security.MmqUser;
import org.monkey.mmq.web.security.MmqUserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * User related methods entry.
 *
 * @author solley
 */
@RestController
@RequestMapping({"/v1/auth", "/v1/auth/users"})
public class UserController {

    @Autowired
    private JwtTokenManager jwtTokenManager;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MmqUserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthConfigs authConfigs;

    @Qualifier("authService")
    @Autowired
    private IMqttAuthService authService;

    /**
     * Update an user.
     *
     * @param username    username of user
     * @param newPassword new password of user
     * @param response http response
     * @param request http request
     * @return ok if update succeed
     * @throws IllegalArgumentException if user not exist or oldPassword is incorrect
     */
    @PutMapping
    public Object updateUser(@RequestParam String username, @RequestParam String newPassword,
                             HttpServletResponse response, HttpServletRequest request) throws IOException {

        User user = userDetailsService.getUserFromDatabase(username);
        if (user == null) {
            throw new IllegalArgumentException("user " + username + " not exist!");
        }

        userDetailsService.updateUserPassword(username, PasswordEncoderUtil.encode(newPassword));

        return RestResultUtils.success("update user ok!");
    }

    /**
     * Get user info.
     *
     * @return Current login user info
     */
    @GetMapping("/info")
    public Object getUsersInfo() {

        return JacksonUtils.toObj("{\n" +
                "    \"id\": \"4291d7da9005377ec9aec4a71ea837f\",\n" +
                "    \"name\": \"MMQ\",\n" +
                "    \"username\": \"Admin\",\n" +
                "    \"password\": \"\",\n" +
                "    \"avatar\": \"/avatar2.jpg\",\n" +
                "    \"status\": 1,\n" +
                "    \"telephone\": \"\",\n" +
                "    \"lastLoginIp\": \"27.154.74.117\",\n" +
                "    \"lastLoginTime\": 1534837621348,\n" +
                "    \"creatorId\": \"admin\",\n" +
                "    \"createTime\": 1497160610259,\n" +
                "    \"merchantCode\": \"TLif2btpzg079h15bk\",\n" +
                "    \"deleted\": 0,\n" +
                "    \"roleId\": \"admin\",\n" +
                "    \"role\": {\n" +
                "\t\"id\": \"admin\",\n" +
                "\t\"name\": \"管理员\",\n" +
                "\t\"describe\": \"拥有所有权限\",\n" +
                "\t\"status\": 1,\n" +
                "\t\"creatorId\": \"system\",\n" +
                "\t\"createTime\": 1497160610259,\n" +
                "\t\"deleted\": 0,\n" +
                "\t\"permissions\": [{\n" +
                "\t\t\"roleId\": \"admin\",\n" +
                "\t\t\"permissionId\": \"dashboard\",\n" +
                "\t\t\"permissionName\": \"仪表盘\",\n" +
                "\t\t\"actions\": \"[]\",\n" +
                "\t\t\"actionEntitySet\": [],\n" +
                "\t\t\"actionList\": null,\n" +
                "\t\t\"dataAccess\": null\n" +
                "\t}, {\n" +
                "\t\t\"roleId\": \"admin\",\n" +
                "\t\t\"permissionId\": \"exception\",\n" +
                "\t\t\"permissionName\": \"异常页面权限\",\n" +
                "\t\t\"actions\": \"[]\",\n" +
                "\t\t\"actionEntitySet\": [],\n" +
                "\t\t\"actionList\": null,\n" +
                "\t\t\"dataAccess\": null\n" +
                "\t}]\n" +
                "}\n" +
                "  }");
    }

    /**
     * Login to solley
     *
     * <p>This methods uses username and password to require a new token.
     *
     * @param username username of user
     * @param password password
     * @param response http response
     * @param request  http request
     * @return new token of the user
     * @throws AccessException if user info is incorrect
     */
    @PostMapping("/login")
    public Object login(@RequestParam String username, @RequestParam String password, HttpServletResponse response,
                        HttpServletRequest request) throws AccessException {

        if (AuthSystemTypes.MMQ.name().equalsIgnoreCase(authConfigs.getMmqAuthSystemType())) {
            MmqUser user = (MmqUser) authService.login(request);

            response.addHeader(MmqAuthConfig.AUTHORIZATION_HEADER, MmqAuthConfig.TOKEN_PREFIX + user.getToken());

            ObjectNode result = JacksonUtils.createEmptyJsonNode();
            result.put(Constants.ACCESS_TOKEN, user.getToken());
            result.put(Constants.TOKEN_TTL, authConfigs.getTokenValidityInSeconds());
            result.put(Constants.GLOBAL_ADMIN, user.isGlobalAdmin());
            result.put(Constants.USERNAME, user.getUsername());
            return result;
        }

        // create Authentication class through username and password, the implement class is UsernamePasswordAuthenticationToken
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                password);

        try {
            // use the method authenticate of AuthenticationManager(default implement is ProviderManager) to valid Authentication
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            // bind SecurityContext to Authentication
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // generate Token
            String token = jwtTokenManager.createToken(authentication);
            // write Token to Http header
            response.addHeader(MmqAuthConfig.AUTHORIZATION_HEADER, "Bearer " + token);
            return RestResultUtils.success("Bearer " + token);
        } catch (BadCredentialsException authentication) {
            return RestResultUtils.failed(HttpStatus.UNAUTHORIZED.value(), null, "Login failed");
        }
    }

    /**
     * Update password.
     *
     * @param oldPassword old password
     * @param newPassword new password
     * @return Code 200 if update successfully, Code 401 if old password invalid, otherwise 500
     */
    @PutMapping("/password")
    @Deprecated
    public RestResult<String> updatePassword(@RequestParam(value = "oldPassword") String oldPassword,
                                             @RequestParam(value = "newPassword") String newPassword) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        User user = userDetailsService.getUserFromDatabase(username);
        String password = user.getPassword();

        // TODO: throw out more fine grained exceptions
        try {
            if (PasswordEncoderUtil.matches(oldPassword, password)) {
                userDetailsService.updateUserPassword(username, PasswordEncoderUtil.encode(newPassword));
                return RestResultUtils.success("Update password success");
            }
            return RestResultUtils.failedWithMsg(HttpStatus.UNAUTHORIZED.value(), "Old password is invalid");
        } catch (Exception e) {
            return RestResultUtils.failedWithMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Update userpassword failed");
        }
    }
}
