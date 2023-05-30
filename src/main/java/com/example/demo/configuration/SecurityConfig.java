package com.example.demo.configuration;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

//import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		  http.csrf().disable();
		  http.cors().disable();
		  http.headers().frameOptions().sameOrigin();
		
			http.authorizeRequests()
							.antMatchers("/pages/login" , "/h2-console/**" , "/pages/logout" ,"/error/**").permitAll()
	            .antMatchers("/js/**", "/css/**", "/vendors/**" , "/images/**").permitAll()
	            .anyRequest().authenticated()
	            .and()
	            .headers()
	            .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
	            .and()
	            // 시큐리티 로그인 로직 
	            .formLogin(login->login
	  	            .loginPage("/pages/login")
	  	            .loginProcessingUrl("/pages/login")
	  	            .defaultSuccessUrl("/pages/template")
	  	            .failureUrl("/error"))
	            .logout()
	            .deleteCookies("JSESSIONID")
	            .logoutUrl("/pages/logout")
	            .logoutSuccessUrl("/pages/login")
	            ;

	    return http.build();
	}
	
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public UserDetailsService userDetailsService(){

        UserDetails admin = User.builder()
                .username("enliple")
                .password(passwordEncoder().encode("1234"))
                .roles("ADMIN")
                .build();
        
        UserDetails user = User.builder()
            .username("user")
            .password(passwordEncoder().encode("1234"))
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }
    
    @Bean
  	CorsConfigurationSource corsConfigurationSource() {
  		CorsConfiguration configuration = new CorsConfiguration();

  		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
  		configuration.setAllowedMethods(Arrays.asList("HEAD","POST","GET","DELETE","PUT"));
  		configuration.setAllowedHeaders(Arrays.asList("*"));
  		configuration.setAllowCredentials(true);
  		
  		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
  		source.registerCorsConfiguration("/**", configuration);
  		return source;
  	}
	
}
