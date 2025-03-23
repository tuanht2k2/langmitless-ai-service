package langmitless.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.dialogflow.v2.*;
import com.kma.common.dto.request.AiResponse;
import com.kma.common.dto.request.AiSearchCourseRequest;
import com.kma.common.dto.response.Response;
import com.kma.common.entity.Account;
import jakarta.annotation.Resource;
import langmitless.ai.enums.EError;
import langmitless.ai.exception.CustomException;
import langmitless.ai.service.interfaces.BusinessServiceProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@Slf4j
public class ChatbotService {
    @Resource
    BusinessServiceProxy businessServiceProxy;

    @Resource
    AuthService authService;

    @Resource
    ObjectMapper objectMapper;

    @Value("${DIALOGFLOW.PROJECT_ID}")
    private String projectId;
    @Value("${DIALOGFLOW.LANGUAGE_ID}")
    private String languageId;

    public Response<Object> ask (String message) {
        try {
            Account account = authService.getCurrentAccount();
            QueryResult result = detectIntent(message, account.getId());
            if (ObjectUtils.isEmpty(result)) {
                log.error("An error happened when get response from dialogFlow");
                return Response.getResponse(500, "An error happened when get response from dialogFlow");
            }
            AiSearchCourseRequest searchCourseRequest = new AiSearchCourseRequest();
            String fulfillmentText = result.getFulfillmentText();
            searchCourseRequest.setDialogResponse(fulfillmentText);
            if (ObjectUtils.isEmpty(fulfillmentText)) {
                Map<String, com.google.protobuf.Value> fields = result.getParameters().getFieldsMap();
                String language = fields.get("language").getStringValue();
                String cost = fields.get("cost").getStringValue();
                String level = fields.get("level").getStringValue();
                searchCourseRequest.setLanguage(language);
                searchCourseRequest.setCost(cost);
                searchCourseRequest.setLevel(Byte.parseByte(level));
            }

            Response<Object> response = businessServiceProxy.searchCourse(searchCourseRequest);
            if (ObjectUtils.isEmpty(response) || !response.getCode().equals(200)) {
                throw new CustomException(EError.SERVICE_ERROR);
            }

            return Response.getResponse(200,"Ask chatbot successfully!");
        } catch (CustomException e) {
            return Response.getResponse(400, e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.getResponse(500, e.getMessage());
        }
    }

    private QueryResult detectIntent(String message, String sessionId) {
        try {
            SessionsClient sessionsClient = SessionsClient.create();
            SessionName session = SessionName.of(projectId, sessionId);

            TextInput.Builder textInput = TextInput.newBuilder()
                    .setText(message)
                    .setLanguageCode(languageId);

            QueryInput queryInput = QueryInput.newBuilder()
                    .setText(textInput)
                    .build();

            DetectIntentRequest request = DetectIntentRequest.newBuilder()
                    .setSession(session.toString())
                    .setQueryInput(queryInput)
                    .build();

            DetectIntentResponse response = sessionsClient.detectIntent(request);

            return response.getQueryResult();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public void dialogFlowListener(Map<String, Object> dialogResponse) {
        try {
            Map<String, Object> queryResult = parseDialogResponse(dialogResponse);
            if (queryResult == null) {
               log.error("Invalid dialog response");
               return;
            }
            AiSearchCourseRequest request = new AiSearchCourseRequest();
            String queryContext = (String) queryResult.get("queryContext");
            String fulfillmentText = (String) queryResult.get("fulfillmentText");
            request.setDialogResponse(fulfillmentText);
            if (fulfillmentText == null) {
                Map<String, Object> parameters = (Map<String, Object>) queryResult.get("parameters");
                request.setLanguage(parameters.get("language").toString());
                request.setCost(parameters.get("cost").toString());
                request.setLevel(Byte.parseByte(parameters.get("level").toString()));
            }

            Response<Object> courseResponse = businessServiceProxy.searchCourse(request);
            log.info(courseResponse.toString());
        } catch (Exception e) {
            log.error("Error when listen dilogFlow signal: {}", e.getMessage());
        }
    }

    private Map<String, Object> parseDialogResponse(Map<String, Object> dialogResponse) {
        if (dialogResponse == null) {
            log.error("Dialog response is null");
            return null;
        }
        return (Map<String, Object>) dialogResponse.get("queryResult");
    }
}
