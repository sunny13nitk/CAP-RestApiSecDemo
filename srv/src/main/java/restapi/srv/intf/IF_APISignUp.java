package restapi.srv.intf;

import cds.gen.db.userlogs.ApiSignUps;
import restapi.exceptions.APISignUpException;
import restapi.pojos.TY_APISignUpCreate;

public interface IF_APISignUp
{
    public ApiSignUps createAPISignUP(TY_APISignUpCreate newAPISignUp) throws APISignUpException;
}
