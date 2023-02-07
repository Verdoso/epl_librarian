package org.greeneyed.epl.librarian.config;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GzzipedJavaScriptServletConfiguration implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
  @Override
  public void customize(ConfigurableServletWebServerFactory factory) {
    MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
    mappings.add("js.gz", "text/javascript");
    factory.setMimeMappings(mappings);
  }

  @Bean
  FilterRegistrationBean<ContentEncodingGzipFilter> contentEncodingGzipFilterFilterRegistration() {
    FilterRegistrationBean<ContentEncodingGzipFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new ContentEncodingGzipFilter());
    registration.addUrlPatterns("*.js.gz");
    registration.setName("ContentEncodingGzipFilter");
    registration.setOrder(1);
    return registration;
  }

  public static class ContentEncodingGzipFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request,
        ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
      HttpServletResponse httpServletResponse = (HttpServletResponse) response;
      httpServletResponse.addHeader("Content-Encoding", "gzip");
      chain.doFilter(request, response);
    }

  }

}
