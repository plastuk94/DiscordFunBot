package discordfunbot;

import java.io.IOException;
import java.net.URL;
import javax.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RestApiRequest {
	private String method;
	private String responseString;
	
	RestApiRequest(URL url, @Nullable String method) throws IOException{
		
		OkHttpClient client = new OkHttpClient();
		
		Request request = new Request.Builder()
				.url(url)
				.build();
		try (Response response = client.newCall(request).execute()) {
			responseString = response.body().string();
		}
	}
	
	String getResponse() {
		return responseString;
	}
	
}
