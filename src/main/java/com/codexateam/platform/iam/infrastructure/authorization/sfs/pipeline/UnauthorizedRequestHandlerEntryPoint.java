package com.codexateam.platform.iam.infrastructure.authorization.sfs.pipeline;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Component that handles unauthorized (401) errors in the security filter chain.
 */
@Component("unauthorizedRequestHandlerEntryPoint")
public class UnauthorizedRequestHandlerEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String json = String.format("{\"error\":\"Unauthorized\",\"path\":\"%s\"}", request.getRequestURI());
        var out = response.getOutputStream();
        out.write(json.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }
}
