package bench.ai.transcribe;

import com.google.common.base.Charsets;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import org.apache.commons.io.FileUtils;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ASREvaluation {

    public static Publisher<EvaluationTask> transform(Flowable<JobPoller.Transcript> upstream) {

        return upstream.flatMap(transcript -> {
            Path hypoPath = Paths.get(EvaluationStart.DATA_PATH, transcript.id(), "hypo.trn");

            String trnTranscript = String.format("%s ( u_1 )\n", transcript.transcript());
            try (PrintWriter out = new PrintWriter(new PrintWriter(hypoPath.toFile()))) {
                out.println(trnTranscript);
            }

            Process process = Runtime.getRuntime().exec(new String[]{"score/run.sh", transcript.id()});
            process.waitFor();
            int exitValue = process.exitValue();

            if (exitValue != 0) {
                //TODO: error handling
                System.out.println("Evaluation job failed: " + transcript.id());
            }

            Optional<EvaluationTask> result = parseEvaluationReport(transcript.id());

            if (result.isPresent()){
                return Flowable.just(result.get());
            } else {
                System.out.println("Fail to complete evaluation task");
                return Flowable.empty();
            }
        });
    }

    public static Pattern rowPattern = Pattern.compile("^\\|[^-=]*\\|$");
    public static final int COR = 0;
    public static final int SUB = 1;
    public static final int DEL = 2;
    public static final int INS = 3;
    public static final int ERR = 4;

    //TODO: very simple parser to parse hypo.trn.sys file
    //TODO: refactor it to be more OOP
    public static Optional<EvaluationTask> parseEvaluationReport(String id) {
        Path resultPath = Paths.get("score/data",id, "hypo.trn.sys");

        try {
            List<String> lines = FileUtils.readLines(resultPath.toFile(), Charsets.UTF_8);
            List<String[]> parsedLines = new ArrayList<>();

            // parse each line
            for (String line : lines){
                String l = line.trim();
                Matcher matcher = rowPattern.matcher(l); // extract table rows

                if (matcher.matches()){
                    parsedLines.add(l.split("\\|"));
                    if (parsedLines.size() == 3) { // result is in the 3rd line
                        String scoresLine = parsedLines.get(2)[3].trim().replaceAll("\\s+", ",");
                        String[] scores = scoresLine.split(",");

                        return Optional.of(new EvaluationTask()
                                .withId(id)
                                .withWer(Float.valueOf(scores[ERR]))
                                .withIns(Float.valueOf(scores[INS]))
                                .withDel(Float.valueOf(scores[DEL]))
                                .withSub(Float.valueOf(scores[SUB])));
                    }
                }
            }

            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    //TODO: remove main function, only used for integration test
    public static void main(String[] args){
        EvaluationTask task = parseEvaluationReport("debug").get();

        System.out.println(String.format("%s %s %s %s", task.getWer(), task.getIns(), task.getDel(), task.getSub()));
    }
}
