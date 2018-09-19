package sourceproject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.Base64;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class LambdaFunctionHandler implements RequestHandler<Request, Response> {
//    private static String API_KEY = decryptAPIKey();
//    private static String SEARCH_ENGINE_ID = decryptSearchEngineID();

    public Response handleRequest(Request input, Context context) {
        context.getLogger().log("Input: " + input + "\n");
        QuerySolver solver = new QuerySolver();

//        context.getLogger().log("Answer: " + solver.solution(input.getQuery(), 0.02));
        return new Response(solver.solution(input.getQuery(), 0.02, System.getenv("apiKey"), System.getenv("searchEngineID")));
    }

    private static String decryptAPIKey() {
        System.out.println("Decrypting key");
        byte[] encryptedKey = Base64.decode(System.getenv("apiKey"));

        AWSKMS client = AWSKMSClientBuilder.defaultClient();

        DecryptRequest request = new DecryptRequest()
                .withCiphertextBlob(ByteBuffer.wrap(encryptedKey));

        ByteBuffer plainTextKey = client.decrypt(request).getPlaintext();
        return new String(plainTextKey.array(), Charset.forName("UTF-8"));
    }

    private static String decryptSearchEngineID() {
        System.out.println("Decrypting key");
        byte[] encryptedKey = Base64.decode(System.getenv("searchEngineID"));

        AWSKMS client = AWSKMSClientBuilder.defaultClient();

        DecryptRequest request = new DecryptRequest()
                .withCiphertextBlob(ByteBuffer.wrap(encryptedKey));

        ByteBuffer plainTextKey = client.decrypt(request).getPlaintext();
        return new String(plainTextKey.array(), Charset.forName("UTF-8"));
    }
}
