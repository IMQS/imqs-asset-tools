package za.co.imqs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerhardv on 2020-02-06.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateDTO {
    private String code;
    private String name;
    private Boolean active;
    private Date dateAdded;
    private Date dateDeactivated;
    private Boolean allowDelete;
    private List<TemplateFieldDTO> fields;

    public TemplateDTO() {
        fields = new ArrayList<>();
    }
}
