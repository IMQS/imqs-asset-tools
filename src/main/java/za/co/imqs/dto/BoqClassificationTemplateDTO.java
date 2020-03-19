package za.co.imqs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerhardv on 2020-02-06.
 */

@Data
@JsonInclude(Include.NON_NULL)
public class BoqClassificationTemplateDTO {

    private List<ClassificationItemDTO> classifications;
    private TemplateDTO template;

    public BoqClassificationTemplateDTO() {

        classifications =  new ArrayList<>();
        template = new TemplateDTO();
    }
}
