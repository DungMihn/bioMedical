package com.spring.bioMedical.config;

import java.io.IOException;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component; // dùng Component thay cho Configuration
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String ctx = request.getContextPath();
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ADMIN_SUPER")) {
            response.sendRedirect(ctx + "/admin-super/admin-branch-list");
            return;
        }
        if (roles.contains("ADMIN_BRANCH")) {
            // ĐƯỜNG DẪN TỒN TẠI trong AdminBranchInvoiceController
            response.sendRedirect(ctx + "/admin-branch/doctor-details");
            return;
        }
        if (roles.contains("DOCTOR")) {
            // TODO: thay bằng URL có thật của bạn
            response.sendRedirect(ctx + "/doctor/index");
            return;
        }
        if (roles.contains("PATIENT")) {
            // TODO: thay bằng URL có thật của bạn
            response.sendRedirect(ctx + "/user/index");
            return;
        }
        response.sendRedirect(ctx + "/showMyLoginPage?error=role");
    }
}
