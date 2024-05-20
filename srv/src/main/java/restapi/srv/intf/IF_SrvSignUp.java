package restapi.srv.intf;

import java.util.List;

import cds.gen.db.userlogs.SrvSignUps;
import restapi.exceptions.APISignUpException;
import restapi.exceptions.InvalidAPIKeyException;
import restapi.pojos.TY_SrvSignUpCreate;
import restapi.pojos.TY_SrvSignUpEdit;

public interface IF_SrvSignUp
{
    public SrvSignUps createSrvSignUP(TY_SrvSignUpCreate newSrvSignUp, String username) throws APISignUpException;

    public List<SrvSignUps> getSrvSignUPs() throws APISignUpException;

    public SrvSignUps getSignUpByAPIKey(String apiKey) throws InvalidAPIKeyException;

    public SrvSignUps updateSignUp(TY_SrvSignUpEdit signUpPayload, String username) throws InvalidAPIKeyException;

}
