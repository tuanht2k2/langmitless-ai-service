package langmitless.ai.controller;

import com.kma.common.dto.request.ChatbotRequest;
import com.kma.common.dto.request.IndexCourseRequest;
import com.kma.common.enums.EChatbotType;
import jakarta.annotation.Resource;
import langmitless.ai.service.EmbeddingService;
import langmitless.ai.service.OpenAiService;
import langmitless.ai.service.PineconeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/ai")
@Slf4j
public class AiController {
    @Resource
    PineconeService pineconeService;

    @Resource
    OpenAiService openAiService;

    @Resource
    EmbeddingService embeddingService;

    @PostMapping("get-response")
    public ResponseEntity<?> getResponse(@RequestBody ChatbotRequest request) {
        return request.getType().equals(EChatbotType.QA) ? openAiService.getResponse(request) : pineconeService.getResponse(request);
    }

    @PostMapping("embedding")
    public ResponseEntity<?> embedding(@RequestBody String input) {
        List<Float> output = embeddingService.getEmbedding(input);
        return ResponseEntity.ok(output.stream().map(String::valueOf).collect(Collectors.joining(", ")));
    }

    @PostMapping("upsert-course-vector")
    public ResponseEntity<?> upsertCourseVector(@RequestBody List<IndexCourseRequest> request) {
        return pineconeService.upsertCoursesVector(request);
    }

//    @PostMapping("query-course")
//    public ResponseEntity<?> queryCourse(@RequestBody String query) {
//        return pineconeService.queryCourseData(query);
//    }
}
