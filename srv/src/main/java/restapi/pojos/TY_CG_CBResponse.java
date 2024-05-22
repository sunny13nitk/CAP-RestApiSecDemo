package restapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TY_CG_CBResponse
{
    private TY_TokenCheck tokenCheck;
    private boolean signupSuccessful;
    private TY_BearerToken clientBearer;
    private String cbMessage;

}
