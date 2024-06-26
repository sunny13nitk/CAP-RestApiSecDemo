package restapi.utilities;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationProperties;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationService;

import lombok.extern.slf4j.Slf4j;
import restapi.pojos.TY_ApplicationDetails;

@Slf4j
public class CL_DestinationUtilities
{
    public static final String GC_tokenServiceURL = "tokenServiceURL";
    public static final String GC_clientId = "clientId";
    public static final String GC_clientSecret = "clientSecret";
    public static final String GC_URL = "URL";
    public static final String GC_code = "code";
    public static final String GC_redirect_uri = "redirect_uri";
    public static final String GC_response_type = "response_type";
    public static final String GC_Header_Location = "location";
    public static final String GC_GrantType = "grant_type";
    public static final String GC_ClientCredentials = "client_credentials";
    public static final String GC_Password = "password";
    public static final String GC_Username = "username";
    public static final String GC_ClientID_Token = "client_id";
    public static final String GC_ClientSecret_Token = "client_secret";
    public static final String GC_BTPProfile = "btp";
    public static final String GC_LocalProfile = "local";

    public static TY_ApplicationDetails getAccessCodeParams4OAuthDestination(String destination) throws Exception
    {
        TY_ApplicationDetails acCodeParms = null;

        Map<String, String> desProps = getOAuth2DesProps(destination);

        if (desProps.size() > 0)
        {
            acCodeParms = new TY_ApplicationDetails();
            String tokenUrl = desProps.get(GC_tokenServiceURL);
            if (StringUtils.hasText(tokenUrl))
            {
                tokenUrl = getValue4mPattern(tokenUrl);
                if (StringUtils.hasText(tokenUrl))
                {
                    acCodeParms.setAuthUrl(tokenUrl);
                }
            }

            String clientId = desProps.get(GC_clientId);
            if (StringUtils.hasText(clientId))
            {
                clientId = getValue4mPattern(clientId);
                if (StringUtils.hasText(clientId))
                {
                    acCodeParms.setClientId(clientId);
                }
            }

            String clientSecret = desProps.get(GC_clientSecret);
            if (StringUtils.hasText(clientSecret))
            {
                clientSecret = getValue4mPattern(clientSecret);
                if (StringUtils.hasText(clientSecret))
                {
                    acCodeParms.setClientSecret(clientSecret);
                }
            }

            String redirect_uri = desProps.get(GC_URL);
            if (StringUtils.hasText(redirect_uri))
            {
                redirect_uri = getValue4mPattern(redirect_uri);
                if (StringUtils.hasText(redirect_uri))
                {
                    acCodeParms.setRedirectUrl(redirect_uri);
                }
            }

            acCodeParms.setResponseType(GC_code);

        }

        return acCodeParms;
    }

    public static Map<String, String> getOAuth2DesProps(String destination) throws Exception
    {

        Map<String, String> desProps = new HashMap<String, String>();

        if (StringUtils.hasText(destination))
        {
            log.info("Scanning for Destination : " + destination);
            log.info("Destination Not bound. Invoking Destination Service..");

            var service = new DestinationService();

            Collection<DestinationProperties> allDestinationProperties = service.getAllDestinationProperties();
            DestinationProperties individualProperties = service.getDestinationProperties(destination);
            if (individualProperties != null)
            {
                if (CollectionUtils.isNotEmpty(allDestinationProperties))
                {

                    individualProperties.getPropertyNames().forEach(p ->
                    {
                        desProps.put(p, individualProperties.get(p).toString());
                    });
                }

            }

        }
        return desProps;
    }

    public static String getValue4mPattern(String value)
    {
        String parsedVal = null;

        Pattern pattern = Pattern.compile("Some\\((.*)\\)");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find())
        {
            parsedVal = matcher.group(1);
        }
        return parsedVal;
    }
}
