package langmitless.ai.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignClientInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = getCurrentToken();
        if (token != null) {
            requestTemplate.header("Authorization", "Bearer " + token);
        }
    }

    private String getCurrentToken() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }
}
