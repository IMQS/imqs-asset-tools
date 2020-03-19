package za.co.imqs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Created by gerhardv on 2020-02-06.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassificationItemDTO {

    private String classificationType;
    private String classificationNodeType;
    private String code;
    private String parentPath;
    private String description;
    private Boolean active;
}
