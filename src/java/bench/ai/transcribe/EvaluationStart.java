package bench.ai.transcribe;

import com.amazonaws.services.transcribe.model.LanguageCode;
import com.amazonaws.services.transcribe.model.Media;
import io.reactivex.Flowable;
import org.apache.commons.io.FileUtils;
import org.reactivestreams.Publisher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class EvaluationStart {

    public static String DATA_PATH = "score/data";

    public interface SingleEvaluationJob extends SingleJob {
        String ref(); // reference transcript
        Media source();
        LanguageCode language();
    }

    public static SingleEvaluationJob getJob(String filename, String bucketname){
        return new EvaluationStart.SingleEvaluationJob(){
            String id = UUID.randomUUID().toString();

            @Override
            public String id() {
                return id;
            }

            @Override
            public String ref() {
                return "down movement and what that does is it will allow the scalar to kind of work its way in between " +
                        "the scales and pull them gently rather than in way that can kind of damage or break the meat";
            }

            @Override
            public Media source() {
                return new Media().withMediaFileUri("s3://" + bucketname + "/" + filename);
            }

            @Override
            public LanguageCode language() {
                return LanguageCode.EnUS;
            }
        };
    }

    public static String test() {
        return "Test";
    }

    public static Publisher<AWSTranscribeCaller.Request> transform(Flowable<SingleEvaluationJob> upstream) {
        return upstream
                .doOnNext(singleEvaluationJob -> {
                    Path jobFoldler = Paths.get(DATA_PATH, singleEvaluationJob.id());
                    jobFoldler.toFile().mkdirs();

                    Path refPath = jobFoldler.resolve("ref.trn");
                    String refTranscript = singleEvaluationJob.ref().replace("\n", " ");
                    String trnTranscript = String.format("%s (u_%s)\n", refTranscript, singleEvaluationJob.id().replace("-", ""));
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
