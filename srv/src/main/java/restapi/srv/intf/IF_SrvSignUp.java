package restapi.srv.intf;

import java.util.List;

import cds.gen.db.userlogs.SrvSignUps;
import restapi.exceptions.APISignUpException;
import restapi.exceptions.InvalidAPIKeyException;
import restapi.pojos.TY_SrvSignUpCreate;

public interface IF_SrvSignUp
{
    public SrvSignUps createSrvSignUP(TY_SrvSignUpCreate newSrvSignUp) throws APISignUpException;

    public List<SrvSignUps> getSrvSignUPs() throws APISignUpException;

    public boolean validateAPIKey(String apiKey) throws InvalidAPIKeyException;
}
