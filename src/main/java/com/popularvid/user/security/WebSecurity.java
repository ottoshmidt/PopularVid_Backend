package com.popularvid.user.security;

import com.popularvid.user.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Web security configuration class.
 *
 * @author Otar Magaldadze
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurity extends WebSecurityConfigurerAdapter {
    private final UserService userService;

    public WebSecurity(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        TokenAuthenticationFilter tokenFilter = new TokenAuthenticationFilter();
        tokenFilter.setUserService(userService);
        http.addFilterBefore(tokenFilter, BasicAuthenticationFilter.class);

        http.csrf().disable();
    }
}
