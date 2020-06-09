package za.co.imqs.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.imqs.dto.BoqClassificationTemplateDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Base64;

/**
 * Created by gerhardv on 2020-02-06.
 */
public class TemplateServiceRestClient {
    private String baseURI;
    private String authSession;
    private final ObjectMapper mapper;

    Logger logger = LoggerFactory.getLogger(TemplateServiceRestClient.class);

    public TemplateServiceRestClient(String baseURI, String authSvcURI, String username, String password, ObjectMapper mapper) throws Exception {
        this.baseURI = baseURI;
        this.mapper = mapper;
        authSession = getAuthSession(authSvcURI, username, password);
    }

    public void createBoqClassification(String boqClassification) throws Exception {
        logger.debug("createBoqClassification ENTER");

        final String restEndpoint = baseURI + "tree";

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            //Define a put request
            HttpPut putRequest = new HttpPut(restEndpoint);

            //Set the API media type and authToken in http content-type header
            putRequest.addHeader("content-type", "application/json");
            putRequest.addHeader("Cookie", authSession);

            //Set the request post body
            StringEntity boqClass = new StringEntity(boqClassification);
            putRequest.setEntity(boqClass);

            //Send the request; It will immediately return the response in HttpResponse object if any
            logger.info("Requesting Endpoint: " + restEndpoint);
            HttpResponse response = httpClient.execute(putRequest);

            //verify the valid error code first
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                throw new RuntimeException("CreateBoqClassification failed with HTTP error code : " + statusCode + ". Endpoint: " + restEndpoint);
            }
        } catch (Exception e) {
            logger.error("Error creating BOQ Classification, requesting endpoint: " + restEndpoint + " - " + e.getMessage(), e);
            throw e;
        }
        logger.debug("createBoqClassification EXIT");
    }

    public void submitClassificationTemplateBatch(List<BoqClassificationTemplateDTO> boqList) throws Exception {
        logger.debug("submitClassificationTemplateBatch ENTER");

        final String restEndpoint = baseURI + "templates";

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            //Define a put request
            HttpPut putRequest = new HttpPut(restEndpoint);

            //Set the API media type and authToken in http content-type header
            putRequest.addHeader("content-type", "application/json");
            putRequest.addHeader("Cookie", authSession);

            //Set the request post body
            logger.debug("PAYLOAD= " + mapper.writeValueAsString(boqList));
            StringEntity boqTemplate = new StringEntity(mapper.writeValueAsString(boqList));
            putRequest.setEntity(boqTemplate);

            //Send the request; It will immediately return the response in HttpResponse object if any
            logger.info("Requesting Endpoint: " + restEndpoint);
            HttpResponse response = httpClient.execute(putRequest);

            //verify the valid error code first
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                throw new RuntimeException("SubmitClassificationTemplateBatch failed with HTTP error code : " + statusCode + ". Endpoint: " + restEndpoint);
            }
        } catch (Exception e) {
            logger.error("Error submitting Classification Template Batch. , requesting endpoint: " + restEndpoint + " - " + e.getMessage(), e);
            throw e;
        }

        logger.debug("submitClassificationTemplateBatch EXIT");
    }

    public String getAuthSession(String authSvcURI, String username, String password) throws Exception {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost postRequest = new HttpPost(authSvcURI + "login");
            postRequest.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
            logger.info("Requesting Endpoint: " + authSvcURI);
            HttpResponse response = httpClient.execute(postRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new RuntimeException(response.getStatusLine().toString());
            }

            return Arrays.toString(response.getHeaders("Set-Cookie"));
        } catch (Exception e) {
            logger.error(String.format("Authorisation failed for username %s. ", username) + " - " + e.getMessage(), e);
            throw e;
        }
    }
}
