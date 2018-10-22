package bench.ai.transcribe;

import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClient;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobResult;
import com.amazonaws.services.transcribe.model.TranscriptionJob;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class JobPoller {
    private static Logger log = LoggerFactory.getLogger(JobPoller.class);

    interface ASRJob extends SingleJob {
        TranscriptionJob transcriptionJob();
    }

    interface Transcript extends SingleJob {
        String transcript();
    }

    public class TranscriptJobFailure extends Exception {}

    public static Publisher<Transcript> transform(Flowable<ASRJob> flowable) {
        AmazonTranscribe transcribe = AmazonTranscribeClient.builder()
                .build();
        ConcurrentLinkedDeque<ASRJob> queue = new ConcurrentLinkedDeque();
        ObjectMapper objectMapper = new ObjectMapper();
        Flowable ticker = Flowable.interval(1, TimeUnit.SECONDS);

        return flowable
                .mergeWith(ticker)
                .flatMap(
                    obj -> {
                        if (obj instanceof ASRJob) {
                            queue.offer(((ASRJob) obj));
                        } else {
                            if (!queue.isEmpty()){
                                ASRJob job = queue.poll();
                                GetTranscriptionJobResult result = transcribe.getTranscriptionJob(
                                        new GetTranscriptionJobRequest()
                                                .withTranscriptionJobName(job.transcriptionJob().getTranscriptionJobName()));
                                String status = result.getTranscriptionJob().getTranscriptionJobStatus();

                                if (status.equals("COMPLETED")) {
                                    String transcriptURL = result.getTranscriptionJob().getTranscript().getTranscriptFileUri();
                                    URL url = new URL(transcriptURL);
                                    BufferedInputStream in = new BufferedInputStream(url.openStream());
                                    //TODO: more robust parsing
                                    JsonNode transcriptJson = objectMapper.readTree(in).get("results").get("transcripts");
                                    String transcriptString = transcriptJson.get(0).get("transcript").asText();
                                    //String transcriptString = transcriptJson.get("results").get("transcripts").get("transcript").asText();
                                    log.info("Get transcript: " + transcriptString);

                                    return Flowable.just(new Transcript() {
                                        String transcript = transcriptString;
                                        String id = job.id();

                                        @Override
                                        public String transcript() {
                                            return transcript;
                                        }

                                        @Override
                                        public String id() {
                                            return id;
                                        }
                                    });
                                } else if (status.equals("FAILED")) {
                                    // swallow errors
                                    //TODO: persist error in database
                                    log.error("Transcript job failed: " + job.transcriptionJob().getFailureReason());
                                } else {
                                    // send job back to queue
                                    queue.offer(job);
                                }
                            }
                        }

                        // by default noting is send to the down stream
                        return Flowable.empty();
                    }
                );
        }
}
