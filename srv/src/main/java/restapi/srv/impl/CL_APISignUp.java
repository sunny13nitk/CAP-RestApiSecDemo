package restapi.srv.impl;

import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import com.sap.cds.Result;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.cqn.CqnInsert;
import com.sap.cds.services.persistence.PersistenceService;

import cds.gen.db.userlogs.ApiSignUps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import restapi.exceptions.APISignUpException;
import restapi.pojos.TY_APISignUpCreate;
import restapi.srv.intf.IF_APISignUp;

@Service
@RequiredArgsConstructor
@Slf4j
public class CL_APISignUp implements IF_APISignUp
{
    private final PersistenceService ps;
    private final String tablePath = "db.userlogs.apiSignUps"; // Table Path - HANA

    @Override
    public ApiSignUps createAPISignUP(TY_APISignUpCreate newAPISignUp) throws APISignUpException
    {
        ApiSignUps signUp = null;
        Result response = null;
        if (newAPISignUp != null && ps != null)
        {
            Map<String, Object> signUpEntity = new HashMap<>();
            signUpEntity.put("ID", UUID.randomUUID()); // ID
            signUpEntity.put("apiKey", UUID.randomUUID()); // API Key
            signUpEntity.put("consumer", newAPISignUp.getConsumer()); // Consumer
            signUpEntity.put("timestamp", new Timestamp(System.currentTimeMillis())); // signedAt

            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            Timestamp ts = new Timestamp(
                    ((java.util.Date) df.parse(newAPISignUp.getValidTo(), new ParsePosition(0))).getTime());

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
                            signUp = response.first(ApiSignUps.class).get();
                        }
                    }
                }
                catch (Exception e)
                {
                    throw new APISignUpException("Error during API sign up. Details - " + e.getLocalizedMessage());
                }
            }
        }

        return signUp;
    }

}
