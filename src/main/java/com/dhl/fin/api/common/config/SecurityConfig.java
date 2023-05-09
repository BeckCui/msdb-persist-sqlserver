package com.dhl.fin.api.common.config;

import com.dhl.fin.api.common.filters.TokenAuthenticationFilter;
import com.dhl.fin.api.common.authentication.TokenAuthenticationProvider;
import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.dto.ApiResponseStatus;
import com.dhl.fin.api.common.filters.ApiVisitLoggerFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Author: yuayuan
 * Email:  ityuan.yuan@dhl.com
 * Date:   10/25/2019
 * Description:
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private TokenAuthenticationProvider tokenAuthenticationProvider;
    private TokenAuthenticationFilter tokenAuthenticationFilter;
    private ApiVisitLoggerFilter apiVisitLoggerFilter;


    @Autowired
    public SecurityConfig(TokenAuthenticationProvider tokenAuthenticationProvider,
                          TokenAuthenticationFilter tokenAuthenticationFilter,
                          ApiVisitLoggerFilter apiVisitLoggerFilter) {
        this.tokenAuthenticationProvider = tokenAuthenticationProvider;
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
        this.apiVisitLoggerFilter = apiVisitLoggerFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().anonymous().and().cors().and().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().
                exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint());
        http.addFilterBefore(tokenAuthenticationFilter, BasicAuthenticationFilter.class);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(tokenAuthenticationProvider);
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            ApiResponse<String> unauthorizedResponse = new ApiResponse<>(ApiResponseStatus.UNAUTHORIZED, authException.getMessage(), null);

            String json = new ObjectMapper().writeValueAsString(unauthorizedResponse);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        };
    }


    @Bean
    public WebMvcConfigurer config() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "OPTIONS", "PUT")
                        .allowedHeaders("*")
                        .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                        .allowCredentials(false);
            }

            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                AntPathMatcher matcher = new AntPathMatcher();
                matcher.setCaseSensitive(false);
                configurer.setPathMatcher(matcher);
            }
        };
    }


}
