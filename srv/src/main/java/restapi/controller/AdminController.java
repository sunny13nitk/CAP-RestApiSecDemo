package restapi.controller;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cds.gen.db.userlogs.ApiSignUps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import restapi.exceptions.APISignUpException;
import restapi.pojos.TY_APISignUpCreate;
import restapi.srv.intf.IF_APISignUp;

@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController
{

    private final IF_APISignUp apiSignUpSrv;

    @PostMapping("/signups")
    public ResponseEntity<EntityModel<ApiSignUps>> createAPISignUp(@RequestBody TY_APISignUpCreate newAPISignUp)
    {
        EntityModel<ApiSignUps> apiSignUp = null;

        if (newAPISignUp != null && apiSignUpSrv != null)
        {
            log.info("Inside API SignUp Processing...");
            try
            {
                ApiSignUps signUp = apiSignUpSrv.createAPISignUP(newAPISignUp);
                if (signUp != null)
                {
                    apiSignUp = EntityModel.of(signUp);
                }
            }
            catch (APISignUpException e)
            {
                throw new APISignUpException(e.getLocalizedMessage());
            }

        }

        return new ResponseEntity<>(apiSignUp, HttpStatus.OK);
    }

}
