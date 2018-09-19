package sourceproject;

public class Request {
    String query;
    public Request(String query) {
        this.query = query;
    }

    public Request() {}
    public String getQuery() {
        return query;
    }

    public void setQuery(String q) {
        query = q;
    }

    @Override
    public String toString() {
        return "MyLambdaRequest [query: " + query + "]";
    }

}
