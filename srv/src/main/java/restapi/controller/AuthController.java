package restapi.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cloud.sdk.cloudplatform.connectivity.CsrfToken;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationAccessException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import restapi.pojos.TY_ApplicationDetails;
import restapi.pojos.TY_BearerToken;
import restapi.pojos.TY_TokenCheck;
import restapi.pojos.TY_TokenRequestBody;
import restapi.srv.intf.IF_APISignUp;
import restapi.utilities.CL_DestinationUtilities;
import restapi.utilities.JWTTokenUtilities;

@RestController
@RequestMapping("/authorize")
@Slf4j
@RequiredArgsConstructor
public class AuthController
{

    private final String desName = "BTP_SVC_INT";

    private final String desNameOAuthCred = "REST_API_BEARER";

    private final IF_APISignUp apiSignUpSrv;

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
    public ResponseEntity<TY_BearerToken> getBearerToken() throws IOException
    {
        TY_BearerToken bearer = null;
        String authCode = null;
        CloseableHttpClient httpClient = null;

        // Prepare Auth Code Url
        try
        {
            TY_ApplicationDetails acCodeParams = CL_DestinationUtilities
                    .getAccessCodeParams4OAuthDestination(desNameOAuthCred);
            if (acCodeParams != null)
            {
                String url = acCodeParams.getAuthUrl() + "?" + CL_DestinationUtilities.GC_clientId + "="
                        + acCodeParams.getClientId() + "&" + CL_DestinationUtilities.GC_redirect_uri + "="
                        + acCodeParams.getRedirectUrl() + "&" + CL_DestinationUtilities.GC_response_type + "="
                        + acCodeParams.getResponseType();
                if (StringUtils.hasText(url))
                {
                    log.info("URL For Access Code : " + url);
                    CloseableHttpResponse response = null;
                    httpClient = HttpClientBuilder.create().build();

                    HttpGet httpGet = new HttpGet(url);

                    // Fire the Url
                    response = httpClient.execute(httpGet);

                    // verify the valid error code first
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != org.apache.http.HttpStatus.SC_OK)
                    {
                        throw new RuntimeException("Failed with HTTP error code : " + statusCode);
                    }
                    else
                    {
                        log.info("Authentication Successful for Auth Code Generation");
                        // Get the header with the name location
                        Header[] headers = response.getHeaders(CL_DestinationUtilities.GC_Header_Location);
                        if (headers.length > 0)
                        {
                            log.info("Location Header Bound...");
                            String[] headerParts = headers[0].toString().split("code=");
                            if (headerParts.length > 0)
                            {
                                authCode = headerParts[headerParts.length - 1];
                                log.info("Auth Code Bound : " + authCode);
                            }
                        }
                    }

                }

            }
        }
        catch (Exception e)
        {
            log.error("Error Accessing Destination :  " + desNameOAuthCred);
            log.error("Error Details :  " + e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }
        finally
        {
            httpClient.close();
        }
        return new ResponseEntity<>(bearer, HttpStatus.OK);

    }

    @GetMapping("/bearerToken")
    public ResponseEntity<TY_BearerToken> getToken(@RequestHeader(name = "apiKey") String apiKey) throws IOException
    {
        TY_ApplicationDetails acCodeParams;
        TY_BearerToken bearer = null;
        CloseableHttpClient httpClient = null;

        // Validate the API Key
        if (apiSignUpSrv != null && StringUtils.hasText(apiKey))
        {
            if (apiSignUpSrv.validateAPIKey(apiKey))
            {
                try
                {
                    acCodeParams = CL_DestinationUtilities.getAccessCodeParams4OAuthDestination(desNameOAuthCred);

                    if (acCodeParams != null)
                    {
                        // Create an HTTP POST request to the token endpoint
                        String url = acCodeParams.getAuthUrl();

                        if (StringUtils.hasText(url) && StringUtils.hasText(acCodeParams.getClientId())
                                && StringUtils.hasText(acCodeParams.getClientSecret()))
                        {

                            TY_TokenRequestBody reqBody = new TY_TokenRequestBody(
                                    CL_DestinationUtilities.GC_ClientCredentials, acCodeParams.getClientId(),
                                    acCodeParams.getClientSecret());
                            if (reqBody != null)
                            {

                                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                                formparams.add(new BasicNameValuePair(CL_DestinationUtilities.GC_GrantType,
                                        CL_DestinationUtilities.GC_ClientCredentials));
                                formparams.add(new BasicNameValuePair(CL_DestinationUtilities.GC_ClientID_Token,
                                        reqBody.getClient_id()));
                                formparams.add(new BasicNameValuePair(CL_DestinationUtilities.GC_ClientSecret_Token,
                                        reqBody.getClient_secret()));

                                httpClient = HttpClientBuilder.create().build();

                                HttpPost httpPost = new HttpPost(url);

                                // Set the request headers
                                httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
                                httpPost.addHeader("Accept", "application/json");

                                // Write the request body
                                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
                                if (entity != null)
                                {

                                    httpPost.setEntity(entity);

                                    // Fire the Url
                                    CloseableHttpResponse response = httpClient.execute(httpPost);

                                    // verify the valid error code first
                                    int statusCode = response.getStatusLine().getStatusCode();
                                    if (statusCode != HttpStatus.OK.value())
                                    {
                                        log.info("Error obtaining Access Token: Http REquest failed with Status  "
                                                + statusCode);
                                        log.info("Error Details :  " + response.getEntity().toString());
                                        return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
                                    }

                                    else
                                    {
                                        // Parse the JSON response
                                        HttpEntity entityResp = response.getEntity();
                                        String apiOutput = EntityUtils.toString(entityResp);

                                        // Conerting to JSON
                                        ObjectMapper mapper = new ObjectMapper();
                                        JsonNode jsonNode = mapper.readTree(apiOutput);
                                        if (jsonNode != null)
                                        {
                                            bearer = new TY_BearerToken();
                                            // Get the access token
                                            String accessToken = jsonNode.get("access_token").asText();
                                            if (StringUtils.hasText(accessToken))
                                            {
                                                bearer.setAccessToken(accessToken);
                                            }

                                            if (StringUtils.hasText(String.valueOf(jsonNode.get("expires_in").asInt())))
                                            {
                                                bearer.setExpiresIn(jsonNode.get("expires_in").asInt());

                                            }

                                            if (StringUtils.hasText(jsonNode.get("scope").asText()))
                                            {
                                                bearer.setScope(jsonNode.get("scope").asText());
                                            }
                                        }

                                    }
                                }

                            }

                        }

                    }
                }

                catch (Exception e)
                {
                    log.info("Error accessing Destination  " + desNameOAuthCred);
                    log.info("Error Details :  " + e.getLocalizedMessage());
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                finally
                {
                    httpClient.close();
                }
            }
        }

        return new ResponseEntity<>(bearer, HttpStatus.OK);
    }

    @GetMapping("/adminToken")
    public ResponseEntity<TY_BearerToken> getToken() throws IOException
    {
        TY_ApplicationDetails acCodeParams;
        TY_BearerToken bearer = null;
        CloseableHttpClient httpClient = null;

        try
        {
            acCodeParams = CL_DestinationUtilities.getAccessCodeParams4OAuthDestination(desNameOAuthCred);

            if (acCodeParams != null)
            {
                // Create an HTTP POST request to the token endpoint
                String url = acCodeParams.getAuthUrl();

                if (StringUtils.hasText(url) && StringUtils.hasText(acCodeParams.getClientId())
                        && StringUtils.hasText(acCodeParams.getClientSecret()))
                {

                    TY_TokenRequestBody reqBody = new TY_TokenRequestBody(CL_DestinationUtilities.GC_ClientCredentials,
                            acCodeParams.getClientId(), acCodeParams.getClientSecret());
                    if (reqBody != null)
                    {

                        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                        formparams.add(new BasicNameValuePair(CL_DestinationUtilities.GC_GrantType,
                                CL_DestinationUtilities.GC_ClientCredentials));
                        formparams.add(new BasicNameValuePair(CL_DestinationUtilities.GC_ClientID_Token,
                                reqBody.getClient_id()));
                        formparams.add(new BasicNameValuePair(CL_DestinationUtilities.GC_ClientSecret_Token,
                                reqBody.getClient_secret()));

                        httpClient = HttpClientBuilder.create().build();

                        HttpPost httpPost = new HttpPost(url);

                        // Set the request headers
                        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
                        httpPost.addHeader("Accept", "application/json");

                        // Write the request body
                        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
                        if (entity != null)
                        {

                            httpPost.setEntity(entity);

                            // Fire the Url
                            CloseableHttpResponse response = httpClient.execute(httpPost);

                            // verify the valid error code first
                            int statusCode = response.getStatusLine().getStatusCode();
                            if (statusCode != HttpStatus.OK.value())
                            {
                                log.info(
                                        "Error obtaining Access Token: Http REquest failed with Status  " + statusCode);
                                log.info("Error Details :  " + response.getEntity().toString());
                                return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
                            }

                            else
                            {
                                // Parse the JSON response
                                HttpEntity entityResp = response.getEntity();
                                String apiOutput = EntityUtils.toString(entityResp);

                                // Conerting to JSON
                                ObjectMapper mapper = new ObjectMapper();
                                JsonNode jsonNode = mapper.readTree(apiOutput);
                                if (jsonNode != null)
                                {
                                    bearer = new TY_BearerToken();
                                    // Get the access token
                                    String accessToken = jsonNode.get("access_token").asText();
                                    if (StringUtils.hasText(accessToken))
                                    {
                                        bearer.setAccessToken(accessToken);
                                    }

                                    if (StringUtils.hasText(String.valueOf(jsonNode.get("expires_in").asInt())))
                                    {
                                        bearer.setExpiresIn(jsonNode.get("expires_in").asInt());

                                    }

                                    if (StringUtils.hasText(jsonNode.get("scope").asText()))
                                    {
                                        bearer.setScope(jsonNode.get("scope").asText());
                                    }
                                }

                            }
                        }

                    }

                }

            }
        }

        catch (Exception e)
        {
            log.info("Error accessing Destination  " + desNameOAuthCred);
            log.info("Error Details :  " + e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        finally
        {
            httpClient.close();
        }

        return new ResponseEntity<>(bearer, HttpStatus.OK);
    }

    @GetMapping("/csrf-token")
    public CsrfToken getCSRFToken(HttpServletRequest request) throws IOException
    {
        return (CsrfToken) request.getAttribute("_csrf");
    }

    // @formatter:off
    /**
     * WE can have a backend mapping of UserName (Technical User Configured);
     *  + Destination that user is entitled to map to 
     *  + also the Previous Token
     *  + Scope(s) the token must carry if any to provide token from here 
     * So Basically the user Credentials payload will trigger a HANA Query to fetch the DEstination 
     * and generate the bearer to API access only if 
     * //For the Current Technical User and Passed Token
     *  -- IF Token pass is mandatory (configuration)
     *  --- prev token verified for Signature
     *  --- prev token has necesary scope(s)
     *  ----- then use Technical User Credentials to generate a Bearer for the request call
     * 
     */
     // @formatter:on

    @GetMapping("/checkToken")
    public ResponseEntity<TY_TokenCheck> validateToken(@RequestHeader(name = "tokenPass") String token)
    {
        TY_TokenCheck tokenInfo = null;
        if (token != null)
        {
            log.info("Token Header Bound");
            String[] chunks = token.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String header = new String(decoder.decode(chunks[0]));
            if (header != null)
            {

                try
                {
                    // Validate Token Signature Using Issued Public Key alongwith it's validity
                    RSAPublicKey rsaKey = JWTTokenUtilities.getRSAPublicKey(header);
                    var algo = Algorithm.RSA256(rsaKey, null);

                    DecodedJWT jwt = JWT.require(algo).build().verify(token);
                    if (jwt != null)
                    {
                        tokenInfo = new TY_TokenCheck();
                        tokenInfo.setValidSignature(true);
                        tokenInfo.setExpired(false);
                    }

                }
                catch (Exception e)
                {
                    tokenInfo = new TY_TokenCheck();
                    if (e instanceof TokenExpiredException)
                    {
                        tokenInfo.setExpired(true);
                    }
                    tokenInfo.setMessage(e.getLocalizedMessage());
                    return new ResponseEntity<>(tokenInfo, HttpStatus.UNAUTHORIZED);
                }

            }

            String payload = new String(decoder.decode(chunks[1]));
            if (payload != null)
            {

                // Parse the decoded header as a JSON object.
                JSONObject jsonPayload = new JSONObject(new String(payload));
                if (jsonPayload != null)
                {
                    tokenInfo.setUserId(jsonPayload.getString("user_name"));
                    tokenInfo.setEmail(jsonPayload.getString("email"));
                    tokenInfo.setExp(jsonPayload.getLong("exp"));

                    JSONArray arrayScopes = jsonPayload.getJSONArray("scope");
                    if (arrayScopes != null)
                    {
                        if (arrayScopes.length() > 0)
                        {
                            List<String> scopesList = new ArrayList<String>();
                            for (int i = 0; i < arrayScopes.length(); i++)
                            {
                                scopesList.add(arrayScopes.getString(i));
                            }
                            tokenInfo.setScopes(scopesList);
                        }
                    }

                }

            }

        }
        return new ResponseEntity<>(tokenInfo, HttpStatus.OK);

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
