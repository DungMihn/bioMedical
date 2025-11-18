package com.spring.bioMedical.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource securityDataSource;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // JDBC auth dùng đúng bảng Users của bạn
        auth.jdbcAuthentication()
                .dataSource(securityDataSource)
                .usersByUsernameQuery("select username, password_hash, enabled from Users where username=?")
                .authoritiesByUsernameQuery("select username, role from Users where username=?")
                .passwordEncoder(passwordEncoder())
                // KHÔNG thêm tiền tố ROLE_ vì DB đã lưu đúng chuỗi quyền (ADMIN_SUPER, ADMIN_BRANCH, ...)
                .rolePrefix("");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // GIỮ encoder demo của bạn (plain text). Lưu ý: KHÔNG an toàn cho production.
        return new PasswordEnconderTest();
    }

 @Override
protected void configure(HttpSecurity http) throws Exception {
    http
        // 1) Bỏ CSRF cho API chat (nếu muốn có thể disable toàn bộ, nhưng mình để giới hạn)
        .csrf()
            .ignoringAntMatchers("/api/chat")
        .and()
        // 2) Phân quyền
        .authorizeRequests()
            // Cho phép API chat truy cập không cần đăng nhập
            .antMatchers("/api/chat").permitAll()

            .antMatchers("/admin-super/**").hasAuthority("ADMIN_SUPER")
            .antMatchers("/admin-branch/**").hasAnyAuthority("ADMIN_SUPER", "ADMIN_BRANCH")
            .antMatchers("/doctor/**").hasAuthority("DOCTOR")
            .antMatchers("/user/**").hasAuthority("PATIENT")

            .antMatchers(
                    "/register", "/confirm", "/showMyLoginPage",
                    "/css/**", "/js/**", "/static/**", "/vendor/**", "/resources/**",
                    "/login/**"
            ).permitAll()
            .anyRequest().authenticated()
        .and()
        .formLogin()
            .loginPage("/showMyLoginPage")
            .loginProcessingUrl("/authenticateTheUser")
            .successHandler(successHandler)
            .permitAll()
        .and()
        .logout().permitAll()
        .and()
        .exceptionHandling()
            .accessDeniedHandler((req, res, ex) ->
                res.sendRedirect(req.getContextPath() + "/access-denied")
            );
}




    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/resources/**", "/login/**", "/static/**", "/Script/**", "/Style/**", "/Icon/**",
                "/js/**", "/vendor/**", "/bootstrap/**", "/Image/**"
        );
    }

    @Bean
    public UserDetailsManager userDetailsManager() {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
        jdbcUserDetailsManager.setDataSource(securityDataSource);
        return jdbcUserDetailsManager;
    }
}

// Encoder demo (plain text) – KHÔNG dùng cho môi trường thật
class PasswordEnconderTest implements PasswordEncoder {

    @Override
    public String encode(CharSequence raw) {
        return raw.toString();
    }

    @Override
    public boolean matches(CharSequence raw, String encoded) {
        return raw.toString().equals(encoded);
    }
}
