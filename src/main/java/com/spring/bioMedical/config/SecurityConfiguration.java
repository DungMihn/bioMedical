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

		  auth.jdbcAuthentication().dataSource(securityDataSource)
    .usersByUsernameQuery("select username, password_hash, enabled from Users where username=?")
    .authoritiesByUsernameQuery("select username, role from Users where username=?")
    .passwordEncoder(passwordEncoder())
    .rolePrefix(""); 
		 } 
	
	@Bean
	public PasswordEncoder passwordEncoder(){
	    return new PasswordEnconderTest();
	}




	@Override
	protected void configure(HttpSecurity http) throws Exception {
            
http
        .csrf().disable()
        .authorizeRequests()
    .antMatchers("/admin/**").hasAuthority("ADMIN")
    .antMatchers("/doctor/**").hasAuthority("DOCTOR")
        
        //  Cho phép đặt lịch không cần đăng nhập
        .antMatchers("/user/slots-by-clinic/**").permitAll()
        .antMatchers("/user/save-app").permitAll()
        .antMatchers("/verify-appointment-otp/**").permitAll()
        .antMatchers("/resend-otp/**").permitAll()
    .antMatchers("/user/**").permitAll()    
    .antMatchers("/test-mail/**").permitAll()    
            .antMatchers("/layout").permitAll()   
    .antMatchers("/register/**").permitAll()    
    .antMatchers("/verify-otp/**").permitAll()  
                .antMatchers("/forgot-password/**").permitAll()    
        .antMatchers("/resend-forgot-otp/**").permitAll()   
        .antMatchers("/verify-forgot-otp/**").permitAll()   
        .antMatchers("/reset-password/**").permitAll()      
        .antMatchers("/resend-otp/**").permitAll()
    .antMatchers("/register").permitAll()
    .antMatchers("/confirm").permitAll()
    .antMatchers("/login/**").permitAll()
    .antMatchers("/css/**", "/js/**", "/static/**", "/vendor/**", "/resources/**").permitAll()
        .antMatchers("/images/**").permitAll()
        .antMatchers("/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.gif").permitAll()
    .anyRequest().authenticated()
    .and()
    .formLogin()
        .loginPage("/showMyLoginPage")
        .loginProcessingUrl("/authenticateTheUser")
        .permitAll()
        .successHandler(successHandler)
    .and()
//    .logout().permitAll()
.logout()
    .logoutUrl("/logout")
    .logoutSuccessHandler((request, response, authentication) -> {
        response.sendRedirect("/user/index");
    })
    .permitAll()

    .and()
    .exceptionHandling().accessDeniedPage("/access-denied");

		 
	}


	
	@Override
	public void configure(WebSecurity web) throws Exception {
	
		web.ignoring().antMatchers("/resources/**","/login/**","/static/**","/Script/**","/Style/**","/Icon/**",
				"/js/**","/vendor/**","/bootstrap/**","/image/**");
		
	}
	

	@Bean
	public UserDetailsManager userDetailsManager() {
		
		JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
		
		jdbcUserDetailsManager.setDataSource(securityDataSource);
		
		return jdbcUserDetailsManager; 
	}
		
	
	
}

class PasswordEnconderTest implements PasswordEncoder {
    @Override
    public String encode(CharSequence charSequence) {
        return charSequence.toString();
    }

    @Override
    public boolean matches(CharSequence charSequence, String s) {
        return charSequence.toString().equals(s);
    }
}
