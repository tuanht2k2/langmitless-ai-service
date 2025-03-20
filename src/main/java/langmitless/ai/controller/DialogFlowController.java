package langmitless.ai.controller;

import jakarta.annotation.Resource;
import langmitless.ai.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/v1/dialogFlow")
public class DialogFlowController {
    @Resource
    private ChatbotService chatbotService;

    @PostMapping("listener")
    public void dialogFlowListener(@RequestBody Map<String, Object> dialogResponse) {
        chatbotService.dialogFlowListener(dialogResponse);
    }
}
