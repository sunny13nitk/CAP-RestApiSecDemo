package restapi.srv.intf;

import java.util.List;

import cds.gen.db.userlogs.ApiSignUps;
import restapi.exceptions.APISignUpException;
import restapi.exceptions.InvalidAPIKeyException;
import restapi.pojos.TY_APISignUpCreate;

public interface IF_APISignUp
{
    public ApiSignUps createAPISignUP(TY_APISignUpCreate newAPISignUp) throws APISignUpException;

    public List<ApiSignUps> getAPISignUPs() throws APISignUpException;

    public boolean validateAPIKey(String apiKey) throws InvalidAPIKeyException;
}
