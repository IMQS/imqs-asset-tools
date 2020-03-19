package za.co.imqs.services;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;

import static java.lang.System.out;


/**
 * Created by gerhardv on 2020-02-06.
 */
public class TemplateServiceRestClient {

    private String baseURI;
    private String username;
    private String password;

    public TemplateServiceRestClient(String baseURI, String username, String password) throws Exception {

         this.baseURI = baseURI;
         this.username = username;
         this.password = password;
    }

    private static void log(String Msg) {

        out.println(Msg);
    }
    
    /**
     * @param boqClassification = BOQ Classification JSON metadata
     */
    public void createBoqClassification(String boqClassification) throws Exception {

        final String restEndpoint = baseURI + "tree";

        HttpClient httpClient = new DefaultHttpClient();
        try {
            //Define a put request
            HttpPut putRequest = new HttpPut(restEndpoint);

            //Set the API media type in http content-type header
            putRequest.addHeader("content-type", "application/json");

            //Set the request post body
            StringEntity boqClass = new StringEntity(boqClassification);
            putRequest.setEntity(boqClass);

            //Send the request; It will immediately return the response in HttpResponse object if any
            HttpResponse response = httpClient.execute(putRequest);

            //verify the valid error code first
            int statusCode = response.getStatusLine().getStatusCode();
            //Debug:remove
            //int statusCode = 201;
            if (statusCode != 201)
            {
                throw new RuntimeException("CreateBoqClassification failed with HTTP error code : " + statusCode + ". Endpoint: " + restEndpoint);
            }
        }
        finally
        {
            //Important: Close the connection
            httpClient.getConnectionManager().shutdown();
        }
    }

    public void submitClassificationTemplateBatch(String batch) throws Exception {

        final String restEndpoint = baseURI + "templates";

        HttpClient httpClient = new DefaultHttpClient();
        try {
            //Define a put request
            HttpPut putRequest = new HttpPut(restEndpoint);

            //Set the API media type in http content-type header
            putRequest.addHeader("content-type", "application/json");

            //Set the request post body
            StringEntity boqTemplate = new StringEntity(batch);
            putRequest.setEntity(boqTemplate);

            //Send the request; It will immediately return the response in HttpResponse object if any
            HttpResponse response = httpClient.execute(putRequest);

            //verify the valid error code first
            int statusCode = response.getStatusLine().getStatusCode();
            //Debug:remove
            //int statusCode = 201;
            if (statusCode != 201)
            {
                throw new RuntimeException("SubmitClassificationTemplateBatch failed with HTTP error code : " + statusCode + ". Endpoint: " + restEndpoint);
            }
        }
        finally
        {
            //Important: Close the connection
            httpClient.getConnectionManager().shutdown();
        }
    }
}
