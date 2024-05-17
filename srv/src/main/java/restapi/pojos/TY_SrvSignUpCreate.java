package restapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TY_SrvSignUpCreate
{
    private String consumer;
    private String xsAppName;
    private String clientId;
    private String sourceScopes;
    private boolean scopeCheckMandatory;
    private String failMessage;
    private String validTo;

}
