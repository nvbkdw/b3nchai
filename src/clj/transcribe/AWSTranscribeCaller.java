package transcribe;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClient;
import com.amazonaws.services.transcribe.model.*;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import org.reactivestreams.Publisher;

public class AWSTranscribeCaller implements FlowableTransformer<AWSTranscribeCaller.Request, JobPoller.ASRJob> {
    AmazonTranscribe transcribe = AmazonTranscribeClient.builder().build();

    @Override
    public Publisher<JobPoller.ASRJob> apply(Flowable<Request> flowable) {
        return flowable.flatMap(request -> {
            StartTranscriptionJobRequest startTranscriptionJobRequest = new StartTranscriptionJobRequest()
                    .withMediaFormat(MediaFormat.Wav)
                    .withLanguageCode(request.language())
                    .withMedia(request.source())
                    .withTranscriptionJobName(request.id());

            StartTranscriptionJobResult result = transcribe.startTranscriptionJob(startTranscriptionJobRequest);

            TranscriptionJob job = result.getTranscriptionJob();

            return Flowable.just(new JobPoller.ASRJob() {
                String id = request.id();
                TranscriptionJob j = job;
                @Override
                public TranscriptionJob transcriptionJob() {
                    return j;
                }

                @Override
                public String id() {
                    return id;
                }
            });
        });
    }

    public interface Request extends SingleJob{
        Media source();
        LanguageCode language();
    }
}
