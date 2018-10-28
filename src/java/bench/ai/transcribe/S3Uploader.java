package bench.ai.transcribe;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class S3Uploader {

    public static Publisher<EvaluationTask> transform(Flowable<EvaluationTask> upstream, AmazonS3Client s3Client, String s3Bucket) {

        String[] resultFiles = new String[]{"hypo.trn", "hypo.trn.pra", "hypo.trn.raw", "hypo.trn.sys", "ref.trn"};

        return upstream.map(task -> {
            Path resultDir = Paths.get("score/data", task.getId());
            String bucket = s3Bucket;
            String s3Path = String.format("s3://%s/%s", bucket, task.getId()); // folder level path

            for (String name : resultFiles){
                File file = resultDir.resolve(name).toFile();
                String key = String.format("%s/%s", task.getId(), name);
                if(file.exists()){
                    PutObjectRequest request = new PutObjectRequest(bucket, key, file);
                    s3Client.putObject(request);
                }
            }
            return task.withS3Path(s3Path);
        });
    }
}
