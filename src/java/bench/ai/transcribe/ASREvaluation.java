package bench.ai.transcribe;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import org.apache.commons.io.FileUtils;
import org.reactivestreams.Publisher;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ASREvaluation {

    interface EvaluationResult {
        String id();
        float wer();
        float rtf();
    }

    public static Publisher<EvaluationResult> transform(Flowable<JobPoller.Transcript> upstream) {

        return upstream.map(transcript -> {
            Path hypoPath = Paths.get(EvaluationStart.DATA_PATH, transcript.id(), "hypo.trn");

            String trnTranscript = String.format("%s (u_%s)\n", transcript.transcript(), transcript.id().replace("-", ""));
            FileUtils.writeStringToFile(hypoPath.toFile(), trnTranscript);

            Process process = Runtime.getRuntime().exec(new String[]{"score/run.sh", transcript.id()});
            process.waitFor();
            int exitValue = process.exitValue();

            if (exitValue != 0) {
                //TODO: error handling
                System.out.println("Evaluation job failed: " + transcript.id());
            }

            return new EvaluationResult() {
                @Override
                public String id() {
                    return transcript.id();
                }

                @Override
                public float wer() {
                    return 0;
                }

                @Override
                public float rtf() {
                    return 0;
                }
            };


        });
    }
}
