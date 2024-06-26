package restapi.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.cds.services.request.UserInfo;

import cds.gen.db.userlogs.ApiSignUps;
import cds.gen.db.userlogs.SrvSignUps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import restapi.exceptions.APISignUpException;
import restapi.pojos.TY_APISignUpCreate;
import restapi.pojos.TY_SrvSignUpCreate;
import restapi.pojos.TY_SrvSignUpEdit;
import restapi.srv.intf.IF_APISignUp;
import restapi.srv.intf.IF_SrvSignUp;

@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController
{

    private final IF_APISignUp apiSignUpSrv;

    private final IF_SrvSignUp srvSignUpSrv;

    private final UserInfo userInfo;

    @PostMapping("/signups")
    public ResponseEntity<EntityModel<ApiSignUps>> createAPISignUp(@RequestBody TY_APISignUpCreate newAPISignUp)
    {
        EntityModel<ApiSignUps> apiSignUp = null;

        if (newAPISignUp != null && apiSignUpSrv != null && userInfo != null)
        {
            log.info("Inside API SignUp Processing...");
            try
            {
                ApiSignUps signUp = apiSignUpSrv.createAPISignUP(newAPISignUp, userInfo.getName());
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

        return new ResponseEntity<>(apiSignUp, HttpStatus.CREATED);
    }

    @GetMapping("/signups")
    public CollectionModel<EntityModel<ApiSignUps>> getAPISignUps()
    {
        CollectionModel<EntityModel<ApiSignUps>> cM = null;
        List<EntityModel<ApiSignUps>> apiSignUpsEMList = null;
        List<ApiSignUps> apiSignUpsList = null;

        if (apiSignUpSrv != null)
        {
            apiSignUpsList = apiSignUpSrv.getAPISignUPs();
            if (CollectionUtils.isNotEmpty(apiSignUpsList))
            {
                apiSignUpsEMList = new ArrayList<EntityModel<ApiSignUps>>();
                for (ApiSignUps signUp : apiSignUpsList)
                {
                    if (signUp != null)
                    {
                        EntityModel<ApiSignUps> eM = EntityModel.of(signUp);
                        // Add link in future for each to do
                        // WebMvcLinkBuilder link4eachPost =
                        // linkTo(methodOn(this.getClass()).getPosts4UserById(userId));
                        // eM.add(link2Posts.withRel(relUserPosts));
                        apiSignUpsEMList.add(eM);
                    }

                }

                cM = CollectionModel.of(apiSignUpsEMList);
            }
        }

        return cM;
    }

    @PostMapping("/srvsignups")
    public ResponseEntity<EntityModel<SrvSignUps>> createSrvSignUp(@RequestBody TY_SrvSignUpCreate newSrvSignUp)
    {
        EntityModel<SrvSignUps> apiSignUp = null;

        if (newSrvSignUp != null && srvSignUpSrv != null && userInfo != null)
        {
            log.info("Inside Srv SignUp Processing...");
            try
            {
                SrvSignUps signUp = srvSignUpSrv.createSrvSignUP(newSrvSignUp, userInfo.getName());
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

        return new ResponseEntity<>(apiSignUp, HttpStatus.CREATED);
    }

    @PutMapping("/srvsignups")
    public ResponseEntity<EntityModel<SrvSignUps>> updateSrvSignUp(@RequestBody TY_SrvSignUpEdit updSrvSignUp)
    {
        EntityModel<SrvSignUps> apiSignUp = null;

        if (updSrvSignUp != null && srvSignUpSrv != null && userInfo != null)
        {
            log.info("Inside Srv SignUp Update Processing...");
            try
            {
                SrvSignUps signUp = srvSignUpSrv.updateSignUp(updSrvSignUp, userInfo.getName());
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

    @GetMapping("/srvsignups")
    public CollectionModel<EntityModel<SrvSignUps>> getSrvSignUps()
    {
        CollectionModel<EntityModel<SrvSignUps>> cM = null;
        List<EntityModel<SrvSignUps>> apiSignUpsEMList = null;
        List<SrvSignUps> apiSignUpsList = null;

        if (apiSignUpSrv != null)
        {
            apiSignUpsList = srvSignUpSrv.getSrvSignUPs();
            if (CollectionUtils.isNotEmpty(apiSignUpsList))
            {
                apiSignUpsEMList = new ArrayList<EntityModel<SrvSignUps>>();
                for (SrvSignUps signUp : apiSignUpsList)
                {
                    if (signUp != null)
                    {
                        EntityModel<SrvSignUps> eM = EntityModel.of(signUp);
                        // Add link in future for each to do
                        // WebMvcLinkBuilder link4eachPost =
                        // linkTo(methodOn(this.getClass()).getPosts4UserById(userId));
                        // eM.add(link2Posts.withRel(relUserPosts));
                        apiSignUpsEMList.add(eM);
                    }

                }

                cM = CollectionModel.of(apiSignUpsEMList);
            }
        }

        return cM;
    }

}
