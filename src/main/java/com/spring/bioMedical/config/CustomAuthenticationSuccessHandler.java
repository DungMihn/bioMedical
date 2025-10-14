package com.spring.bioMedical.config;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Custom handler để redirect sau khi đăng nhập thành công
 */
@Configuration
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // Ưu tiên ADMIN_SUPER
        if (roles.contains("ADMIN_SUPER")) {
            response.sendRedirect("/admin-super/admin-branch-list");
            return;
        }

        // ADMIN_BRANCH
        if (roles.contains("ADMIN_BRANCH")) {
            response.sendRedirect("/admin-branch/doctor-details");
            return;
        }

        // DOCTOR
        if (roles.contains("DOCTOR")) {
            response.sendRedirect("/doctor/index");
            return;
        }

        // PATIENT
        if (roles.contains("PATIENT")) {
            response.sendRedirect("/user/index");
            return;
        }

        // fallback nếu role không hợp lệ
        response.sendRedirect("/showMyLoginPage?error=role");
    }
}
