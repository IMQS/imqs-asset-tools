package za.co.imqs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Created by gerhardv on 2020-02-06.
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateFieldDTO {
    private String fieldName;
    private String defaultValue;
    private String dataType;
    private Boolean mandatory;
    private String validation;
    private String description;
    private String options[];
}
