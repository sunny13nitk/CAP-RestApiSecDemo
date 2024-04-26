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
    public static final String GC_ClientID_Token = "client_id";
    public static final String GC_ClientSecret_Token = "client_secret";
    public static final String GC_BTPProfile = "btp";
    public static final String GC_LocalProfile = "local";
    public static final String GC_VKEY = "-----BEGIN PUBLIC KEY-----"
            + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1RBXewdBMp6J19y4cTB9"
            + "Cwd8MzxfBdJdHX12fr1igbWGllXPbq4Q+Mdd7qi8BPAqVSgl7xQtL+FuE/x3Lcw6"
            + "GgB3JxgMFCpAKGu25lHI/zyYUOjB7z9CMPtWyKTdqLV5/2j5Bviy/+w2X9fWpXJp"
            + "YzV0BGroOs6XRZ5/dtwbS5C4Cv2WGAczX2a2LuzWcwQ/aC9u0PRx47EUduW/Fd/V"
            + "E7AHiwwBs2vnwGIN6jTRrchNXwpw/pqrzO0g77/OoykTqx7sb7WYX+nueks2FxWe"
            + "hC1NPKJSxyIikobtplDCuEpX50PlgeIOEcQdMk3/+VTUBMVBFhFZjdQb4P5lRWhZ" + "yQIDAQAB"
            + "-----END PUBLIC KEY-----";

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

    public static RSAPublicKey getRSAPublicKey() throws CertificateException
    {
        // var decode = Base64.getDecoder().decode(GC_VKEY.getBytes());
        var certificate = CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(GC_VKEY.getBytes()));
        var publicKey = (RSAPublicKey) certificate.getPublicKey();
        return publicKey;
    }

    public static RSAPublicKey getPublicKey(String algorithm) throws Exception
    {
        // Get the public key from the header.
        // String pemEncodedPublicKey = header.split(",")[1].split(":")[1].replace("\"",
        // "");
        // byte[] decodedPublicKey = Base64.getDecoder().decode(GC_VKEY);
        // X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedPublicKey);

        String publicKeyPEM = GC_VKEY.replace("-----BEGIN PUBLIC KEY-----", "");
        publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
        publicKeyPEM = publicKeyPEM.replace("\n", "");

        // Decode the contents of the file from Base64
        byte[] base64EncodedKeyBytes = java.util.Base64.getDecoder().decode(publicKeyPEM);

        // Convert the contents of the file to a RSAPublicKey object
        X509EncodedKeySpec spec = new X509EncodedKeySpec(base64EncodedKeyBytes);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(spec);

        // SecretKeySpec secretKeySpec = new
        // SecretKeySpec(CL_DestinationUtilities.GC_VKEY.getBytes(), algorithm);
        // KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        // PublicKey publicKey = keyFactory.generatePublic(secretKeySpec);
        return publicKey;
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
