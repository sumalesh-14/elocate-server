package com.elocate.elocate.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Filter to clean up JSON request bodies.
 * Specifically handles the "Unexpected character (' ' (code 160))" error 
 * by replacing non-breaking spaces with regular spaces.
 */
@Component
public class JsonCleanupFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpServletRequest) {
            String contentType = httpServletRequest.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                chain.doFilter(new JsonCleanupRequestWrapper(httpServletRequest), response);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }

    private static class JsonCleanupRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;

        public JsonCleanupRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            String rawBody = request.getReader().lines().collect(Collectors.joining("\n"));
            // Replace non-breaking space (code 160 / \u00a0) with regular space
            String cleanedBody = rawBody.replace('\u00a0', ' ');
            this.body = cleanedBody.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() throws IOException {
                    return byteArrayInputStream.read();
                }

                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // Not implemented
                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
