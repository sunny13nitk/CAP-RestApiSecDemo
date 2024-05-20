package restapi.srv.impl;

import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sap.cds.Result;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.cqn.CqnInsert;
import com.sap.cds.services.persistence.PersistenceService;

import cds.gen.db.userlogs.SrvSignUps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import restapi.exceptions.APISignUpException;
import restapi.exceptions.InvalidAPIKeyException;
import restapi.pojos.TY_SrvSignUpCreate;
import restapi.srv.intf.IF_SrvSignUp;

@Service
@Slf4j
@RequiredArgsConstructor
public class CL_SrvSignUp implements IF_SrvSignUp
{

    private final String tablePath = "db.userlogs.srvSignUps"; // Table Path - HANA

    private final PersistenceService ps;

    @Override
    public SrvSignUps createSrvSignUP(TY_SrvSignUpCreate newSrvSignUp) throws APISignUpException
    {

        SrvSignUps signUp = null;

        Result response = null;
        if (newSrvSignUp != null && ps != null)
        {
            if (StringUtils.hasText(newSrvSignUp.getClientId()) && StringUtils.hasText(newSrvSignUp.getXsAppName())
                    && StringUtils.hasText(newSrvSignUp.getConsumer()))
            {

                if (newSrvSignUp.isScopeCheckMandatory())
                {
                    if (!StringUtils.hasText(newSrvSignUp.getSourceScopes()))
                    {
                        throw new APISignUpException(
                                "No source scopes defined to fulfill Mandatory Role Checks from Source App - "
                                        + newSrvSignUp.getXsAppName());
                    }
                }

                Map<String, Object> signUpEntity = new HashMap<>();
                signUpEntity.put("ID", UUID.randomUUID()); // ID
                signUpEntity.put("apiKey", UUID.randomUUID()); // API Key
                signUpEntity.put("consumer", newSrvSignUp.getConsumer()); // Consumer
                signUpEntity.put("xsappname", newSrvSignUp.getXsAppName()); // Application Name
                signUpEntity.put("clientId", newSrvSignUp.getClientId()); // Client Id
                signUpEntity.put("sourceScopes", newSrvSignUp.getSourceScopes()); // source Scopes
                signUpEntity.put("isScopeCheckMandatory", newSrvSignUp.isScopeCheckMandatory()); // source Scopes Check
                signUpEntity.put("failMessage", newSrvSignUp.getFailMessage()); // Message retun in Case of failure

                signUpEntity.put("signedAt", new Timestamp(System.currentTimeMillis())); // signedAt

                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

                Timestamp ts = new Timestamp(
                        ((java.util.Date) df.parse(newSrvSignUp.getValidTo(), new ParsePosition(0))).getTime());

                log.info("Valid Till : " + ts);

                signUpEntity.put("validTill", ts); // Valid To
                signUpEntity.put("isActive", true); // isActive

                if (signUpEntity != null)
                {

                    try
                    {
                        CqnInsert qInsert = Insert.into(this.tablePath).entry(signUpEntity);
                        if (qInsert != null)
                        {
                            log.info("SignUp  Insert Query Bound!");
                            Result result = ps.run(qInsert);
                            if (result.list().size() > 0)
                            {
                                log.info("# SignUp Successfully Completed - " + result.rowCount());
                                response = result;
                                signUp = response.first(SrvSignUps.class).get();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        throw new APISignUpException("Error during API sign up. Details - " + e.getLocalizedMessage());
                    }
                }

            }
            else
            {
                throw new APISignUpException(
                        "Error during Service sign up. Client Id or Application name or Consumer name missing in Payload ");
            }

        }

        return signUp;
    }

    @Override
    public List<SrvSignUps> getSrvSignUPs() throws APISignUpException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSrvSignUPs'");
    }

    @Override
    public boolean validateAPIKey(String apiKey) throws InvalidAPIKeyException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateAPIKey'");
    }

}
