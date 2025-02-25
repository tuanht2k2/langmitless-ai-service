package langmitless.ai.service;

import com.kma.common.dto.request.ChatbotRequest;
import com.kma.common.dto.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OpenAiService {
    public ResponseEntity<?> getResponse (ChatbotRequest request) {
        return ResponseEntity.ok(Response.getResponse(200, "Success"));
    }
}
