package restapi.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationAccessException;

import lombok.extern.slf4j.Slf4j;
import restapi.pojos.TY_AccessCodeParams;
import restapi.pojos.TY_BearerToken;
import restapi.utilities.CL_DestinationUtilities;

@RestController
@RequestMapping("/authorize")
@Slf4j
public class AuthController
{

    private final String desName = "BTP_SVC_INT";

    private final String desNameOAuthCred = "REST_API_AUTH_CODE";

    @GetMapping("/basic")
    public ResponseEntity<Map<String, String>> accessPublicEndpoint()
    {

        log.info("Welcome to public zone");
        Map<String, String> desProps = null;

        if (StringUtils.hasText(desName))
        {
            log.info("Scanning for Destination : " + desName);
            log.info("Destination Not bound. Invoking Destination Service..");
            try
            {
                desProps = getDestinationDetails(desName);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return new ResponseEntity<>(desProps, HttpStatus.OK);
    }

    @GetMapping("/oAuth2")
    public ResponseEntity<Map<String, String>> accessOAuth2Endpoint()
    {

        log.info("Welcome to public zone");
        Map<String, String> desProps = new HashMap<String, String>();

        try
        {
            desProps = CL_DestinationUtilities.getOAuth2DesProps(desNameOAuthCred);
        }
        catch (Exception e)
        {
            log.error("Error Accessing Destination :  " + desNameOAuthCred);
            log.error("Error Details :  " + e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }

        return new ResponseEntity<>(desProps, HttpStatus.OK);
    }

    @GetMapping("/bearer")
    public ResponseEntity<TY_BearerToken> getBearerToken()
    {
        TY_BearerToken bearer = null;

        // Prepare Auth Code Url
        try
        {
            TY_AccessCodeParams acCodeParams = CL_DestinationUtilities
                    .getAccessCodeParams4OAuthDestination(desNameOAuthCred);
            if (acCodeParams != null)
            {
                String url = acCodeParams.getAuthUrl() + "?" + CL_DestinationUtilities.GC_clientId + "="
                        + acCodeParams.getClientId() + "&" + CL_DestinationUtilities.GC_redirect_uri + "="
                        + acCodeParams.getRedirectUrl() + "&" + CL_DestinationUtilities.GC_response_type + "="
                        + acCodeParams.getResponseType();
                log.info(url);

            }
        }
        catch (Exception e)
        {
            log.error("Error Accessing Destination :  " + desNameOAuthCred);
            log.error("Error Details :  " + e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<>(bearer, HttpStatus.OK);

    }

    private Map<String, String> getDestinationDetails(String destinationName) throws Exception
    {
        Map<String, String> desProps = new HashMap<String, String>();
        try
        {
            Destination dest = DestinationAccessor.getDestination(destinationName);
            if (dest != null)
            {
                log.info("Destination Bound via Destination Accessor.");
                log.info("Accessing Properties for Destination...");
                dest.getPropertyNames().forEach(p ->
                {
                    desProps.put(p, dest.get(p).get().toString());
                }

                );

            }
        }
        catch (DestinationAccessException e)
        {

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            log.error("Error Accessing Destination : " + e.getLocalizedMessage());
            log.error("Stack trace Details: " + stackTrace);
            throw new Exception("Not able to connect to the Destination : " + e.getLocalizedMessage());

        }

        return desProps;
    }

}
