package za.co.imqs.importers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.imqs.dto.BoqClassificationTemplateDTO;
import za.co.imqs.dto.ClassificationItemDTO;
import za.co.imqs.dto.TemplateDTO;
import za.co.imqs.dto.TemplateFieldDTO;
import za.co.imqs.exceptions.MetadataMissingException;
import za.co.imqs.services.TemplateServiceRestClient;
import java.io.File;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by gerhardv on 2020-02-04.
 * Imports a V6 PolicyVAR csv file into the V8 templates DB via the input form service.
 *
 *  boq-import-config.json example:
 *   {
 *   	"imqs-template-service": {
 *   		"baseURI": "http://192.168.1.65:8668/template/v1_0/",
 *   	    "authSvcURI": "http://192.168.1.65:/auth2/login",
 *   		"username": "imqs",
 *   		"password": "password"
 *          },
 *   	"batchSize": 5,
 *   	"importFilename": "D:\\Dump\\IMQS\\Exports\\test\\export_java.csv",
 *   	"fieldList": {
 *   		"eul": "NUMERIC",
 *   		"extentUnit": "STRING",
 *   		"eulUnit": "STRING",
 *   		"eulCG": "NUMERIC",
 *   		"residualPct": "NUMERIC",
 *   		"residualPctCG": "NUMERIC",
 *   		"unitRate": "NUMERIC",
 *   		"refUnitRateUnit": "STRING",
 *   		"descriptorSizeUnit": "STRING",
 *   		"extentConversion": "NUMERIC",
 *   		"unitRateCG": "NUMERIC",
 *   		"IncludeDescriptorSize": "BOOLEAN",
 *   		"CRCCalcIncludeDescriptorSize": "BOOLEAN",
 *   		"InterpolateDescriptorSize": "STRING",
 *   		"InterpolationTolerancePercentage": "NUMERIC",
 *   		"refDepreciationMethodId": "STRING"
 *      }
 *   }
 */

public class BoqImporter {
    private static final String BOQ_CLASSIFICATION_CODE = "BOQ";
    private static final String BOQ_CLASSIFICATION_ROOT_CODE = "BOQ_ROOT";
    private static final String BOQ_FINYEAR_CODE = "BOQ_FY";
    private static final String BOQ_COMPONENT_TYPE_CODE = "BOQ_CT";
    private static final String BOQ_DESCRIPTOR_TYPE_CODE = "BOQ_DT";
    private static final String BOQ_DESCRIPTOR_CLASS_CODE = "BOQ_DC";
    private static final String BOQ_DESCRIPTOR_SIZE_CODE = "BOQ_DS";

    private final String filename;
    private final int batchSize;                        //Max size of the batch before sending to the template service
    @SuppressWarnings("unchecked")
    private final Map<String, String> fieldList;        //list of fields to import as defined in the config (excluding the descriptor fields)
    private final TemplateServiceRestClient templateSvc;
    private final ObjectMapper mapper;

    Logger logger = LoggerFactory.getLogger(BoqImporter.class);

    public BoqImporter(String configFilename) throws Exception {
        //setup the configuration
        mapper = new ObjectMapper();

        final JsonNode config = mapper.readTree(new File(configFilename));
        filename = config.get("importFilename").asText();
        batchSize = config.get("batchSize").asInt(50);
        //get the fields to import from the config
        final JsonNode fList = config.get("fieldList");
        fieldList = mapper.readValue(fList.toString(), Map.class);

        //get the template and auth service details
        final JsonNode svcConfig = config.get("imqs-template-service");
        templateSvc = new TemplateServiceRestClient(svcConfig.get("baseURI").asText(),
                                                    svcConfig.get("authSvcURI").asText(),
                                                    svcConfig.get("username").asText(),
                                                    svcConfig.get("password").asText(),
                                                    mapper);
    }

    public void execute() {
        try {
            logger.info("Importing BOQ Policy Template. (" + filename + ")   Batch size: " + batchSize);

            //Read the BOQ Classification metadata and create the BOQ classification
            String boqClassificationMetadata = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("classificationtype-boq.json"), StandardCharsets.UTF_8);
            if (boqClassificationMetadata == null) {
                throw new MetadataMissingException("BOQ Classification metatdata resource failed to load.");
            }
            templateSvc.createBoqClassification(boqClassificationMetadata);

            //Open the import file
            try (final Reader reader = Files.newBufferedReader(Paths.get(filename));
                 final CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                         .withFirstRecordAsHeader()
                         .withIgnoreHeaderCase()
                         .withTrim())) {
                long lineCount = 0;
                //Create the BOQ template array based on the batch size as specified in the config.
                final List<BoqClassificationTemplateDTO> boqBatch = new ArrayList<>(batchSize);

                //Parse the import file
                for (CSVRecord csvRecord : csvParser) {
                    //Create a new ClassificationTemplate item to populate
                    final BoqClassificationTemplateDTO boqItem = new BoqClassificationTemplateDTO();

                    //Create Classification nodes for the BOQ Root and BOQ FinYear
                    final String finYear = csvRecord.get("financialYear");
                    boqItem.getClassifications().add(newClassificationItem(BOQ_CLASSIFICATION_ROOT_CODE, BOQ_CLASSIFICATION_CODE, "NULL", "Bill of Quantities Root"));
                    boqItem.getClassifications().add(newClassificationItem(BOQ_FINYEAR_CODE, finYear, BOQ_CLASSIFICATION_CODE, finYear));

                    //Create the Classification Descriptor nodes
                    //We Build up the template name by concatenating the descriptions of the descriptor fields
                    StringBuilder templateName = new StringBuilder();
                    boqItem.getClassifications().add(newDescriptorClassificationItem(csvRecord, "componentType", BOQ_COMPONENT_TYPE_CODE, 2, templateName));
                    boqItem.getClassifications().add(newDescriptorClassificationItem(csvRecord, "descriptorType", BOQ_DESCRIPTOR_TYPE_CODE, 3, templateName));
                    boqItem.getClassifications().add(newDescriptorClassificationItem(csvRecord, "descriptorClass", BOQ_DESCRIPTOR_CLASS_CODE, 4, templateName));
                    boqItem.getClassifications().add(newDescriptorClassificationItem(csvRecord, "descriptorSize", BOQ_DESCRIPTOR_SIZE_CODE, 5, templateName));

                    //Create the Template fields
                    populateTemplate(csvRecord, boqItem, templateName.toString());

                    //Add the new item to the BOQ list
                    boqBatch.add(boqItem);

                    lineCount = csvRecord.getRecordNumber();

                    //Check if we reach the batchSize. If true then send to the Template service
                    if (boqBatch.size() == batchSize) {
                        submitBatch(boqBatch, lineCount);
                    }
                }
                //Handle the last few records in case it is less than the batch size and we are done reading the file
                if (boqBatch.size() > 0) {
                    submitBatch(boqBatch, lineCount);
                }
            }
            logger.info("BOQ Policy Template import complete for file " + filename + "");
        } catch (Exception e) {
            logger.error("BoqImporter completed with errors. " + e.getMessage(), e);
        }
    }

    private TemplateFieldDTO newTemplateField(CSVRecord csvRecord, String fieldName, String dataType) {
        final String value = csvRecord.get(fieldName);

        //Only add fields that do have values
        if (value.equals("NULL") || value.isEmpty()) {
            return null;
        } else {
            final TemplateFieldDTO fieldDTO = new TemplateFieldDTO();
            fieldDTO.setFieldName(fieldName);
            fieldDTO.setDefaultValue(value);
            fieldDTO.setDataType(dataType);
            fieldDTO.setMandatory(false);
            fieldDTO.setValidation(null);
            fieldDTO.setDescription(null);

            return fieldDTO;
        }
    }

    private void populateTemplate(CSVRecord csvRecord, BoqClassificationTemplateDTO boqCT, String templateName) {
        final TemplateDTO templateDTO = boqCT.getTemplate();
        //Use the boqPath for the template code as it "should" be unique
        templateDTO.setCode(csvRecord.get("boqPath"));
        templateDTO.setName(templateName);
        templateDTO.setActive(true);
        templateDTO.setDateAdded(null);
        templateDTO.setDateDeactivated(null);
        templateDTO.setAllowDelete(false);

        //Add all the template fields
        final List<TemplateFieldDTO> fields = templateDTO.getFields();
        fieldList.forEach((key, value) -> {
            final TemplateFieldDTO tfDTO;
            //descriptorSize is already been used as part of the key and contains the unit as well, thus
            // for the pure value we are using descriptorSizeValue
            if (key.equals("descriptorSizeValue")) {
                tfDTO = newTemplateField(csvRecord, "descriptorSize", value);
            }
            else {
                tfDTO = newTemplateField(csvRecord, key, value);
            }
            
            if (tfDTO != null) {
                fields.add(tfDTO);
            }
        });
    }

    private ClassificationItemDTO newClassificationItem(String classificationNodeType, String code, String parentPath, String description) {
        final ClassificationItemDTO itemDTO = new ClassificationItemDTO();
        itemDTO.setClassificationType(BOQ_CLASSIFICATION_CODE);
        itemDTO.setClassificationNodeType(classificationNodeType);
        itemDTO.setCode(code);
        itemDTO.setParentPath(parentPath);
        itemDTO.setDescription(description);
        itemDTO.setActive(true);

        return itemDTO;
    }

    private ClassificationItemDTO newDescriptorClassificationItem(CSVRecord csvRecord, String fieldName, String nodeTypeCode, int delimiterIndex, StringBuilder templateName) {
        //The code column is appended with "_ID"
        String code = csvRecord.get(fieldName + "_ID");
        String description = csvRecord.get(fieldName);
        final String path = csvRecord.get("boqPath");
        //parse out the parent path  from the boqPath based on the index of the delimiter of the descriptor field
        final String parentPath = path.substring(0, StringUtils.ordinalIndexOf(path, "-", delimiterIndex));

        if (description.isEmpty()) {
            code = "NULL";
            description = "Not Specified";
        } else {
            //Build up the template name by concatenating the descriptions of the descriptor fields
            templateName = (templateName.length() == 0) ? (templateName.append(description)) : (templateName.append(" ").append(description));
        }

        return newClassificationItem(nodeTypeCode, code, parentPath, description);
    }

    private void submitBatch(List<BoqClassificationTemplateDTO> boqList, long lineCount) throws Exception {
        templateSvc.submitClassificationTemplateBatch(boqList);
        boqList.clear();
        logger.info("Batch complete - records processed = " + lineCount);
    }
}