package za.co.imqs.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.imqs.dto.BoqClassificationTemplateDTO;
import java.util.List;

/**
 * Created by gerhardv on 2020-02-06.
 */
public class TemplateServiceRestClient {
    private String baseURI;

    Logger logger = LoggerFactory.getLogger(TemplateServiceRestClient.class);

    public TemplateServiceRestClient(String baseURI, String username, String password) throws Exception {
         this.baseURI = baseURI;
    }

    public void createBoqClassification(String boqClassification) throws Exception {
        logger.debug("createBoqClassification ENTER");

        final String restEndpoint = baseURI + "tree";

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
            //Define a put request
            HttpPut putRequest = new HttpPut(restEndpoint);

            //Set the API media type in http content-type header
            putRequest.addHeader("content-type", "application/json");

            //Set the request post body
            StringEntity boqClass = new StringEntity(boqClassification);
            putRequest.setEntity(boqClass);

            //Send the request; It will immediately return the response in HttpResponse object if any
            logger.debug("Requesting Endpoint: " + restEndpoint);
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
        catch (Exception e) {
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

            //Set the API media type in http content-type header
            putRequest.addHeader("content-type", "application/json");

            //Set the request post body
            final ObjectMapper mapper = new ObjectMapper();
            StringEntity boqTemplate = new StringEntity(mapper.writeValueAsString(boqList));
            putRequest.setEntity(boqTemplate);

            //Send the request; It will immediately return the response in HttpResponse object if any
            logger.debug("Requesting Endpoint: " + restEndpoint);
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
        catch (Exception e) {
            logger.error("Error submitting Classification Template Batch. , requesting endpoint: " + restEndpoint + " - " + e.getMessage(), e);
            throw e;
        }

        logger.debug("submitClassificationTemplateBatch EXIT");
    }
}
