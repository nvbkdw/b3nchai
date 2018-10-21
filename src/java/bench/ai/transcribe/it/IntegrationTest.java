package bench.ai.transcribe.it;

import bench.ai.transcribe.ASREvaluation;
import bench.ai.transcribe.AWSTranscribeCaller;
import bench.ai.transcribe.EvaluationStart;
import bench.ai.transcribe.JobPoller;
import com.amazonaws.services.transcribe.model.LanguageCode;
import com.amazonaws.services.transcribe.model.Media;
import io.reactivex.Flowable;
import io.reactivex.processors.ReplayProcessor;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class IntegrationTest {
    public static void main(String[] args) throws InterruptedException {
        Logger log = LoggerFactory.getLogger(IntegrationTest.class);
        ReplayProcessor processor = ReplayProcessor.create();
        Publisher evaluationStart = new EvaluationStart().apply(processor);
        Publisher callTranscribe = new AWSTranscribeCaller().apply(Flowable.fromPublisher(evaluationStart));
        Publisher pollJob = new JobPoller().apply(Flowable.fromPublisher(callTranscribe).observeOn(Schedulers.io()));
        Publisher evaluation = new ASREvaluation().apply(Flowable.fromPublisher(pollJob).observeOn(Schedulers.computation()));
        CountDownLatch latch = new CountDownLatch(1);
        Flowable.fromPublisher(evaluation).subscribe(e -> {}, throwable -> {}, () -> {latch.countDown();});


        processor.onNext(new EvaluationStart.SingleEvaluationJob(){
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
                return new Media().withMediaFileUri("s3://benchai/test.wav");
            }

            @Override
            public LanguageCode language() {
                return LanguageCode.EnUS;
            }
        });

        latch.await();
    }
}
