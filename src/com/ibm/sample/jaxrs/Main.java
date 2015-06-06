package com.ibm.sample.jaxrs;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Main {

	public static void main(String[] args) throws Exception {
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
				"a509e5db-db17-4d7e-b5f1-3bf23468dff1", "n7CGKCkzGcSU");
		provider.setCredentials(AuthScope.ANY, credentials);

		// assume a self signed certificate which doesn't have a host name that
		// matches
		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				builder.build(),
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		CloseableHttpClient client = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setDefaultCredentialsProvider(provider).build();

		// create the HTTP Post operation
		HttpPost httpPost = new HttpPost(
				"https://gateway.watsonplatform.net/question-and-answer-beta/api/v1/question/healthcare");

		// create the HTTP Post Body information (How to build this comes from
		// the documentation)
		StringEntity ent = new StringEntity("{ \"question\" : {\"questionText\" : \"What is diabetes\"} }");
		ent.setContentType("application/json");
		httpPost.setEntity(ent);
		httpPost.setHeader("X-SyncTimeOut", "30");

		// execute
		HttpResponse response = client.execute(httpPost);

		System.out.println(EntityUtils.toString(response.getEntity()));

		// return the response
		// return Response.status(response.getStatusLine().getStatusCode())
		// .header("Pragma", "no-cache")
		// .header("Cache-Control", "no-cache")
		// .entity(EntityUtils.toString(response.getEntity())).build();

	}

}

// {
// "question_and_answer": [
// {
// "name": "Question and Answer-lx",
// "label": "question_and_answer",
// "plan": "question_and_answer_free_plan",
// "credentials": {
// "url": "https://gateway.watsonplatform.net/question-and-answer-beta/api",
// "username": "a509e5db-db17-4d7e-b5f1-3bf23468dff1",
// "password": "n7CGKCkzGcSU"
// }
// }
// ]
// }
