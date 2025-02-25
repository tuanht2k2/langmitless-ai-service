package langmitless.ai.service;

import com.google.protobuf.Struct;
import com.kma.common.dto.request.ChatbotRequest;
import com.kma.common.dto.request.IndexCourseRequest;
import com.kma.common.dto.response.Response;
import com.kma.common.enums.EChatbotType;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import jakarta.annotation.Resource;
import langmitless.ai.enums.EError;
import langmitless.ai.enums.EPineconeNamespace;
import langmitless.ai.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PineconeService {
    @org.springframework.beans.factory.annotation.Value("${PINECONE.API_KEY}")
    private String API_KEY;

    @org.springframework.beans.factory.annotation.Value("${PINECONE.INDEX}")
    private String INDEX;

    private final Pinecone pinecone;
    private final Index index;

    public PineconeService() {
        this.pinecone = new Pinecone.Builder("pcsk_6o1LLJ_GmaXgEctVnucswk7F8dbsACXqCfAJi7vWRrix9T5URjqr87YwhR2U5tc8FAGzPD").build();
        this.index = pinecone.getIndexConnection("langmitless");
    }

    @Resource
    EmbeddingService embeddingService;

    public ResponseEntity<?> upsertCoursesVector(List<IndexCourseRequest> indexCourseRequestList) {
        try {
            for (IndexCourseRequest indexCourseRequest : indexCourseRequestList) {
                Map<String, String> metadata = new HashMap<>();
                metadata.put("id", indexCourseRequest.getId());
                metadata.put("name", indexCourseRequest.getName());
                metadata.put("language", indexCourseRequest.getLanguage());
                metadata.put("cost", indexCourseRequest.getCost().toString());

                String embeddingInput = indexCourseRequest.getName() +
                        ", name: " +
                        indexCourseRequest.getLanguage() +
                        ", price: " +
                        indexCourseRequest.getCost().toString() +
                        ", level: " +
                        indexCourseRequest.getLevel() +
                        ", topics: " +
                        indexCourseRequest.getTopics() +
                        ", description: " +
                        indexCourseRequest.getDescription();
                List<Float> embeddingOutput = embeddingService.getEmbedding(embeddingInput);

                index.upsert(indexCourseRequest.getId(), embeddingOutput, null, null, convertMapToStruct(metadata), EPineconeNamespace.COURSE.toString());
            }

            return ResponseEntity.ok("Upsert record successful!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.ok(Response.getResponse(500, e.getMessage()));
        }
    }

//    public List<CourseResponse> queryCourseData (String query) {
//        try {
//            List<Float> embeddingOutput = embeddingService.getEmbedding(query);
//            QueryResponseWithUnsignedIndices queryResponse = index.queryByVector(5, embeddingOutput, EPineconeNamespace.COURSE.toString());
//            List<CourseResponse> courseResponse = new ArrayList<>();
//            if (queryResponse != null && !queryResponse.getMatchesList().isEmpty()) {
//                courseResponse = queryResponse.getMatchesList().stream()
//                        .map(match -> {
//                            Struct metadataStruct = match.getMetadata();
//                            log.info(metadataStruct.toString());
//                            String id = metadataStruct.getFieldsOrDefault("id", Value.getDefaultInstance()).getStringValue();
//                            String name = metadataStruct.getFieldsOrDefault("name", Value.getDefaultInstance()).getStringValue();
//                            String description = metadataStruct.getFieldsOrDefault("description", Value.getDefaultInstance()).getStringValue();
//                            String language = metadataStruct.getFieldsOrDefault("language", Value.getDefaultInstance()).getStringValue();
//
//                            long cost = metadataStruct.containsFields("cost")
//                                    ? (long) metadataStruct.getFieldsOrDefault("cost", Value.getDefaultInstance()).getNumberValue()
//                                    : 0L;
//
//                            return new CourseResponse(id, name, description, cost, language);
//                        })
//                        .collect(Collectors.toList());
//            }
//
//            return courseResponse;
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            throw new CustomException(EError.SERVICE_ERROR);
//        }
//    }

    public List<String> queryCourseData (String query) {
        try {
            List<Float> embeddingOutput = embeddingService.getEmbedding(query);
            QueryResponseWithUnsignedIndices queryResponse = index.queryByVector(5, embeddingOutput, EPineconeNamespace.COURSE.toString());
            List<String> courseIds = new ArrayList<>();
            if (queryResponse != null && !queryResponse.getMatchesList().isEmpty()) {
                courseIds = queryResponse.getMatchesList().stream()
                        .filter(match -> match.getScore() > 0.4)
                        .map(ScoredVectorWithUnsignedIndices::getId)
                        .collect(Collectors.toList());

                log.info("Filtered Course IDs: {}", courseIds);
            }

            return courseIds;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(EError.SERVICE_ERROR);
        }
    }

    public ResponseEntity<?> getResponse (ChatbotRequest request) {
        try {
            Response<Object> response = new Response<>();
            response.setCode(200);
            response.setMessage("Get response successful!");
            if (request.getType().equals(EChatbotType.COURSE)) {
                response.setData(queryCourseData(request.getContent()));
            } else {

            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("error when get response: {}", e.getMessage());
            return ResponseEntity.ok(Response.getResponse(500, e.getMessage()));
        }
    }

    private Struct convertMapToStruct(Map<String, String> metadata) {
        Struct.Builder structBuilder = Struct.newBuilder();
        metadata.forEach((key, value) ->
                structBuilder.putFields(key, com.google.protobuf.Value.newBuilder().setStringValue(value).build()));
        return structBuilder.build();
    }
}