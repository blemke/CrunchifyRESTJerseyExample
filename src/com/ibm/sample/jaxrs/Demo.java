package com.ibm.sample.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
import org.apache.wink.json4j.JSON;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

@javax.ws.rs.ApplicationPath("resources")
@Path("/test")
public class Demo extends javax.ws.rs.core.Application {

  @Context
  private UriInfo context;

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response questionWatson(@FormParam("question") String question) throws Exception {
    String userid = null, password = null, restServerURL = null;
    //Find my service from VCAP_SERVICES in BlueMix
    String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
    String Service_Name  = "Watson QAAPI-0.1";

    System.out.println("VCAP SERVICES is :"+VCAP_SERVICES);

    // Get the Service Credentials for Watson QAAPI
   if (VCAP_SERVICES != null &&
     ((JSONObject)JSON.parse(VCAP_SERVICES)).containsKey(Service_Name)    ) {
       try {
         JSONObject obj = (JSONObject)JSON.parse(VCAP_SERVICES);
         JSONArray service = obj.getJSONArray(Service_Name);

         // retrieve the service information
         JSONObject catalog = service.getJSONObject(0);

         // retrieve the credentials
         JSONObject credentials = catalog.getJSONObject("credentials");

         // get the credential contents
         userid =    credentials.getString("username");
         password =  credentials.getString("password");
         restServerURL = credentials.getString("url");

       } catch (NullPointerException | JSONException e) {
         e.printStackTrace();
       }
     } else {
       // add logging
       System.out.println("VCAP_SERVICES does not contain the service.");
       // generic error condition
       return Response.status(Response.Status.BAD_REQUEST).header("Pragma", "no-cache")
                .header("Cache-Control", "no-cache")
                .entity("{\"error\" : \"Watson service information not found.\"}").build();
     }

     // create credential information for security
     CredentialsProvider provider = new BasicCredentialsProvider();
     UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userid, password);
     provider.setCredentials(AuthScope.ANY, credentials);

     // assume a self signed certificate which doesn't have a host name that matches
     SSLContextBuilder builder = new SSLContextBuilder();
     builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
     SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
       builder.build(),SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
     CloseableHttpClient client = HttpClients.custom().
                                  setSSLSocketFactory(sslsf).
                                  setDefaultCredentialsProvider(provider).
                                  build();

     // create the HTTP Post operation
     HttpPost httpPost = new HttpPost(restServerURL);

     // create the HTTP Post Body information (How to build this comes from the documentation)
     StringEntity ent = new StringEntity("{\"question\" : {\"questionText\" : \"" + question + "\"}}");
     ent.setContentType("application/json");
     httpPost.setEntity(ent);
     httpPost.setHeader("X-SyncTimeOut", "30");

     //execute
     HttpResponse response = client.execute(httpPost);

     // return the response
     return Response.status(response.getStatusLine().getStatusCode())
              .header("Pragma", "no-cache")
              .header("Cache-Control", "no-cache")
              .entity(EntityUtils.toString(response.getEntity()))
              .build();

  }
}
      