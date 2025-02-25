package langmitless.ai.service;

import ai.djl.Application;
import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.huggingface.translator.TextEmbeddingTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.translate.Translator;
import langmitless.ai.enums.EError;
import langmitless.ai.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class EmbeddingService {
    private final Model model;
    private final Translator<String, float[]> translator;

        public EmbeddingService() throws IOException, ModelException {
            HuggingFaceTokenizer tokenizer = HuggingFaceTokenizer.newInstance("sentence-transformers/all-MiniLM-L6-v2");
            this.model = ModelZoo.loadModel(
                    Criteria.builder()
                            .setTypes(String.class, float[].class)
                            .optApplication(Application.NLP.TEXT_EMBEDDING)
                            .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
                            .optTranslator(TextEmbeddingTranslator.builder(tokenizer).optNormalize(true).build())
                            .optEngine("PyTorch")
                            .build()
            );

            this.translator = TextEmbeddingTranslator.builder(tokenizer).optNormalize(true).build();
        }

        public List<Float> getEmbedding(String text) {
            try (var predictor = model.newPredictor(translator)) {
                float[] embeddingArray = predictor.predict(text);

                return IntStream.range(0, embeddingArray.length)
                        .mapToObj(i -> embeddingArray[i])
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.info("Error when get embedding: {}", e);
                throw new CustomException(EError.BAD_REQUEST);
            }
        }
}
