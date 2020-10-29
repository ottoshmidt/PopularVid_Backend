package com.popularvid.user.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.popularvid.user.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that verifies security tokens and assigns roles to users.
 *
 * @author Otar Magaldadze
 */
public class TokenAuthenticationFilter extends GenericFilterBean {
    private static final Logger LOGGER = LogManager.getLogger();

    private JWTVerifier verifier;

    /**
     * We need to access some of the UserService class functionality.
     *
     * WebSecurity class sends the object for us. This ordering is required
     * by security chain loading.
     *
     * @param userService UserService class instance.
     */
    void setUserService(UserService userService) {
        verifier = JWT.require(userService.getAlgorithm())
                .withIssuer(UserService.issuer)
                .build();
    }

    /**
     * Currently only one role, ROLE_USER, is implemented.
     *
     * @param request http request wrapper
     * @param response http response wrapper
     * @param chain filter chain instance
     * @throws IOException i/o exception
     * @throws ServletException servlet exception
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String ip = httpRequest.getRemoteAddr();

        LOGGER.trace("Client IP address " + ip);

        String authHeader = httpRequest.getHeader("Authorization");
        String token = null;

        if (authHeader != null) {
            var parts = httpRequest.getHeader("Authorization").split(" ");
            if (parts.length == 2) {
                token = parts[1];
            }
        }

        LOGGER.trace("JTW token " + token);

        if (token != null) {
            DecodedJWT jwt = null;

            try {
                jwt = verifier.verify(token);
            } catch (JWTVerificationException e) {
                LOGGER.trace("JWT verify error: " + e.getMessage());
            }

            String id = "0";
            String role = "ROLE_ANONYMOUS";

            if (jwt != null) {
                id = jwt.getClaim("id").asString();

                role = "ROLE_USER";
            }

            setCurrentUser(id, role);
        }

        chain.doFilter(request, response);
    }

    /**
     * Set currently authenticated user to security context.
     *
     * @param userId User's database integer ID as string
     * @param role e.g. ROLE_USER, ROLE_ANONYMOUS.
     */
    private void setCurrentUser(String userId, String role) {
        List<GrantedAuthority> authorities = new ArrayList<>(1);

        authorities.add(new SimpleGrantedAuthority(role));

        User user = new User(
                userId,
                "",
                true,
                true,
                true,
                true,
                authorities);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
