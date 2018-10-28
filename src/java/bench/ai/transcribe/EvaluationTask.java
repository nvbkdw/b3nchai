package bench.ai.transcribe;

public class EvaluationTask {
    String id;
    float wer;
    float ins;
    float del;
    float sub;
    String s3Path;

    public String getId() {
        return id;
    }

    public float getWer() {
        return wer;
    }

    public float getIns() {
        return ins;
    }

    public float getDel() {
        return del;
    }

    public float getSub() {
        return sub;
    }

    public String getS3Path() {
        return s3Path;
    }

    public EvaluationTask withId(String id) {
        this.id = id;
        return this;
    }

    public EvaluationTask withWer(float wer) {
        this.wer = wer;
        return this;
    }

    public EvaluationTask withIns(float ins) {
        this.ins = ins;
        return this;
    }

    public EvaluationTask withDel(float del) {
        this.del = del;
        return this;
    }

    public EvaluationTask withSub(float sub) {
        this.sub = sub;
        return this;
    }

    public EvaluationTask withS3Path(String s3Path) {
        this.s3Path = s3Path;
        return this;
    }
}
