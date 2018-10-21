package bench.ai.transcribe;

import com.amazonaws.services.transcribe.model.LanguageCode;
import com.amazonaws.services.transcribe.model.Media;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import org.apache.commons.io.FileUtils;
import org.reactivestreams.Publisher;

import java.nio.file.Path;
import java.nio.file.Paths;

public class EvaluationStart implements FlowableTransformer<EvaluationStart.SingleEvaluationJob, AWSTranscribeCaller.Request> {

    public static String DATA_PATH = "score/data";

    public interface SingleEvaluationJob extends SingleJob {
        String ref(); // reference transcript
        Media source();
        LanguageCode language();
    }

    @Override
    public Publisher<AWSTranscribeCaller.Request> apply(Flowable<SingleEvaluationJob> upstream) {
        return upstream
                .doOnNext(singleEvaluationJob -> {
                    Path jobFoldler = Paths.get(DATA_PATH, singleEvaluationJob.id());
                    jobFoldler.toFile().mkdirs();

                    Path refPath = jobFoldler.resolve("ref.trn");
                    String refTranscript = singleEvaluationJob.ref().replace("\n", " ");
                    String trnTranscript = String.format("%s (u_%s)", refTranscript, singleEvaluationJob.id().replace("-", ""));
                    FileUtils.writeStringToFile( refPath.toFile(), trnTranscript);
                })
                .map(singleEvaluationJob -> {
                    return new AWSTranscribeCaller.Request() {
                        @Override
                        public String id() {
                            return singleEvaluationJob.id();
                        }

                        @Override
                        public Media source() {
                            return singleEvaluationJob.source();
                        }

                        @Override
                        public LanguageCode language() {
                            return singleEvaluationJob.language();
                        }
                    };
                });
    }
}
