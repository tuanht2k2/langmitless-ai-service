package langmitless.ai.service.interfaces;

import com.kma.common.dto.request.AiSearchCourseRequest;
import com.kma.common.dto.request.EditMessageRequest;
import com.kma.common.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "business-service")
@Component
public interface BusinessServiceProxy {
    @PostMapping("api/v1/courses/ai-search")
    Response<Object> searchCourse(@RequestBody AiSearchCourseRequest request);

    @PostMapping("business/chatbot/chatbot-listener")
    Response<Object> chatbotListener(@RequestBody EditMessageRequest request);
}
