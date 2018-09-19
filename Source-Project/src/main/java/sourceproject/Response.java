package sourceproject;

public class Response {
    double answer;
    public Response(double answer) {
        this.answer = answer;
    }

    public Response() {}
    public double getAnswer() {
        return answer;
    }

    public void setAnswer(double a) {
        answer = a;
    }

    @Override
    public String toString() {
        return "MyLambdaResponse [answer=" + answer + "]";
    }
}
