package restapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TY_SrvSignUpEdit
{
    private String apiKey;
    private String sourceScopes;
    private boolean scopeCheckMandatory;
    private String failMessage;
    private String validTo;
    private boolean active;
}
