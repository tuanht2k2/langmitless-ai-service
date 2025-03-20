package langmitless.ai.controller;

import com.kma.common.dto.response.Response;
import jakarta.annotation.Resource;
import langmitless.ai.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("chatbot")
public class ChatbotController {
    @Resource
    private ChatbotService chatbotService;

    @PostMapping("ask")
    public Response<Object> getResponse(@RequestBody String input) {
        return chatbotService.ask(input);
    }
}
