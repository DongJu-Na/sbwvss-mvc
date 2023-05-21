package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http.authorizeRequests()
	            .antMatchers("/pages/login").permitAll()
	            .antMatchers("/js/**", "/css/**", "/vendor/**" , "/images/**").permitAll()
	            .anyRequest().authenticated()
	            .and()
	            .csrf().ignoringRequestMatchers(
	                new AntPathRequestMatcher("/h2-console/**"))
	            .and()
	            .headers()
	            .addHeaderWriter(new XFrameOptionsHeaderWriter(
	                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
	            .and()
	            .formLogin()
	            .loginPage("/pages/login")
	            .defaultSuccessUrl("/")
	            .and()
	            .logout()
	            .logoutSuccessUrl("/pages/login")
	            ;

	    return http.build();
	}
	
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
}
