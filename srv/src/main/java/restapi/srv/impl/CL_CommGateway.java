package restapi.srv.impl;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cds.gen.db.userlogs.SrvSignUps;
import io.jsonwebtoken.lang.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import restapi.exceptions.ClientBearerException;
import restapi.exceptions.InvalidAPIKeyException;
import restapi.exceptions.JWTTokenException;
import restapi.pojos.TY_ApplicationDetails;
import restapi.pojos.TY_BearerToken;
import restapi.pojos.TY_CG_CBPL;
import restapi.pojos.TY_CG_CBResponse;
import restapi.pojos.TY_CG_TokenPassInfo;
import restapi.pojos.TY_TokenCheck;
import restapi.pojos.TY_TokenRequestBody;
import restapi.srv.intf.IF_CommGateway;
import restapi.srv.intf.IF_SrvSignUp;
import restapi.utilities.CL_DestinationUtilities;
import restapi.utilities.JWTTokenUtilities;

@Service
@RequiredArgsConstructor
@Slf4j
public class CL_CommGateway implements IF_CommGateway
{

    private final IF_SrvSignUp srvSignUpSrv;

    @Value("${rest-destination}")
    private String restDestination;

    @Override
    public TY_CG_CBResponse getClientBearer(TY_CG_CBPL cbPayload) throws ClientBearerException
    {

        TY_CG_CBResponse cbResponse = null;
        SrvSignUps srvSignUpInfo = null;
        TY_CG_TokenPassInfo cgTokenInfo = null;

        if (cbPayload != null && srvSignUpSrv != null)
        {
            if (!StringUtils.hasText(cbPayload.getApiKey()))
            {
                throw new ClientBearerException("Api Key is mandatory to generate Client Bearer!");
            }

            if (!StringUtils.hasText(cbPayload.getPassToken()))
            {
                throw new ClientBearerException("passToken from Consuming App is mandatory to generate Client Bearer!");
            }

            try
            {
                srvSignUpInfo = verifyApiKey(cbPayload.getApiKey());
                if (srvSignUpInfo != null)
                {
                    // Validate signUp is Active and has Validity beyond Current Time Stamp
                    if (srvSignUpInfo.getIsActive() && (srvSignUpInfo.getValidTill().isAfter(Instant.now())))
                    {
                        // Proceed
                        cbResponse = new TY_CG_CBResponse();
                        try
                        {
                            cbResponse.setTokenCheck(this.verifyPassToken(cbPayload.getPassToken()));

                            // Verify Token Sign Up
                            if (!cbResponse.getTokenCheck().isValidSignature()
                                    || cbResponse.getTokenCheck().isExpired())
                            {
                                // Invalid Pass Token
                                throw new ClientBearerException(
                                        "Token (passToken) from consumer App is invalid! Details : "
                                                + cbResponse.getTokenCheck().getMessage());
                            }
                            else
                            {
                                // Proceed
                                cgTokenInfo = this.extractTokenInfo(cbPayload.getPassToken());
                                if (cgTokenInfo != null)
                                {
                                    cgTokenInfo.setApiKey(cbPayload.getApiKey());
                                    cgTokenInfo.setRoles(cbResponse.getTokenCheck().getScopes());

                                    try
                                    {
                                        if (this.verifyClientSignUp(srvSignUpInfo, cgTokenInfo))
                                        {
                                            cbResponse.setSignupSuccessful(true);

                                            // Trigger Bearer Issue
                                            TY_BearerToken clientToken;
                                            try
                                            {
                                                clientToken = getClientBearerToken();
                                                if (clientToken == null)
                                                {
                                                    throw new ClientBearerException(srvSignUpInfo.getFailMessage()
                                                            + " Client Bearer could not be obtained for REST API destination..");
                                                }
                                                else
                                                {

                                                    cbResponse.setClientBearer(clientToken);
                                                    cbResponse.setSignupSuccessful(true);
                                                    return cbResponse;
                                                }

                                            }
                                            catch (IOException e)
                                            {
                                                throw new ClientBearerException(srvSignUpInfo.getFailMessage()
                                                        + " Client Bearer could not be obtained for REST API destination.. - Details : "
                                                        + e.getLocalizedMessage());
                                            }

                                        }
                                    }
                                    catch (ClientBearerException e)
                                    {
                                        throw new ClientBearerException(srvSignUpInfo.getFailMessage() + "Details : "
                                                + e.getLocalizedMessage());
                                    }

                                }
                                else
                                {
                                    throw new ClientBearerException(
                                            "Error fetching Issuer Information (passToken) from consumer App. Check registration configuration for API Key : "
                                                    + cbPayload.getApiKey());
                                }
                            }
                        }
                        catch (JWTTokenException e)
                        {
                            throw new ClientBearerException(srvSignUpInfo.getFailMessage()
                                    + "Error verifying (passToken) from consumer App. Details : "
                                    + e.getLocalizedMessage());
                        }
                    }
                    else
                    {
                        throw new ClientBearerException("Registration for API Key - " + cbPayload.getApiKey()
                                + " has expired or is currently Inactive. ");
                    }

                }

            }
            catch (InvalidAPIKeyException e)
            {
                throw new ClientBearerException("Error fetching registration information for apiKey : "
                        + cbPayload.getApiKey() + "Details - " + e.getLocalizedMessage());
            }

        }

        return cbResponse;

    }

    @Override
    public SrvSignUps verifyApiKey(String apiKey) throws InvalidAPIKeyException
    {
        SrvSignUps srvSignUp = null;
        if (srvSignUpSrv != null)
        {

            try
            {
                srvSignUp = srvSignUpSrv.getSignUpByAPIKey(apiKey);

            }
            catch (InvalidAPIKeyException e)
            {
                throw e;
            }
        }
        return srvSignUp;
    }

    @Override
    public TY_CG_TokenPassInfo extractTokenInfo(String passToken) throws JWTTokenException
    {
        TY_CG_TokenPassInfo tokenInfo = null;

        String[] chunks = passToken.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(chunks[1]));
        if (payload != null)
        {

            // Parse the decoded header as a JSON object.
            JSONObject jsonPayload = new JSONObject(new String(payload));
            if (jsonPayload != null)
            {
                tokenInfo.setClientId(jsonPayload.getString("client_id"));

                JSONArray arrayaud = jsonPayload.getJSONArray("aud");
                if (arrayaud != null)
                {
                    if (arrayaud.length() > 0)
                    {
                        List<String> audList = new ArrayList<String>();
                        for (int i = 0; i < arrayaud.length(); i++)
                        {
                            audList.add(arrayaud.getString(i));
                        }
                        tokenInfo.setAud(audList);
                    }
                }
            }

        }

        return tokenInfo;
    }

    @Override
    public TY_TokenCheck verifyPassToken(String passToken) throws JWTTokenException
    {
        TY_TokenCheck tokenInfo = null;
        if (StringUtils.hasText(passToken))
        {
            if (passToken != null)
            {
                log.info("Token Header Bound");
                String[] chunks = passToken.split("\\.");
                Base64.Decoder decoder = Base64.getUrlDecoder();

                String header = new String(decoder.decode(chunks[0]));
                if (header != null)
                {

                    try
                    {
                        // Validate Token Signature Using Issued Public Key alongwith it's validity
                        RSAPublicKey rsaKey = JWTTokenUtilities.getRSAPublicKey(header);
                        var algo = Algorithm.RSA256(rsaKey, null);

                        DecodedJWT jwt = JWT.require(algo).build().verify(passToken);
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
                        else
                        {
                            tokenInfo.setValidSignature(false);
                        }
                        tokenInfo.setMessage(e.getLocalizedMessage());
                        return tokenInfo;
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

        }

        return tokenInfo;
    }

    @Override
    public boolean verifyClientSignUp(SrvSignUps signUp, TY_CG_TokenPassInfo passTokenInfo) throws ClientBearerException
    {
        boolean validSignUp = true;

        if (signUp != null && passTokenInfo != null)
        {
            // App Name within Audience of token Issuers
            if (StringUtils.hasText(signUp.getXsappname()) && CollectionUtils.isNotEmpty(passTokenInfo.getAud()))
            {
                Optional<String> appNameinAudO = passTokenInfo.getAud().stream()
                        .filter(a -> a.equals(signUp.getXsappname())).findFirst();
                if (appNameinAudO.isPresent())
                {
                    // Client ID Comparison b/w sign Up and Pass Token
                    if (StringUtils.hasText(signUp.getClientId()) && StringUtils.hasText(passTokenInfo.getClientId()))
                    {
                        if (!signUp.getClientId().equalsIgnoreCase(passTokenInfo.getClientId()))
                        {
                            throw new ClientBearerException("Registration for ApiKey : " + signUp.getApiKey()
                                    + " has clientId  " + " which could not be found in passToken.");
                        }
                        else
                        {
                            // Scopes Mandatory Check
                            // If Enabled each scope found in config should be there in Roles of pass Token
                            if (signUp.getIsScopeCheckMandatory())
                            {
                                // Get Scopes from Sign Up in a list
                                List<String> signUpScopes = Arrays.asList(signUp.getSourceScopes().split(","));
                                if (CollectionUtils.isNotEmpty(signUpScopes))
                                {
                                    for (String signUpScope : signUpScopes)
                                    {
                                        Optional<String> scopeO = passTokenInfo.getRoles().stream()
                                                .filter(s -> s.equals(signUpScope)).findFirst();
                                        if (!scopeO.isPresent())
                                        {
                                            throw new ClientBearerException("scope  " + signUpScope
                                                    + " could not be found in passToken Scope(s)!  Verity Registration of ApiKey : "
                                                    + signUp.getApiKey());
                                        }
                                    }
                                }
                                else
                                {
                                    throw new ClientBearerException(
                                            "scope Checks made mandatory but no source scope(s) specified in registration for ApiKey : "
                                                    + signUp.getApiKey());
                                }
                            }
                        }
                    }
                    else
                    {
                        throw new ClientBearerException(
                                "clientId not maintained in Service registration or Pass Token clientId is blank");
                    }

                }
                else
                {
                    throw new ClientBearerException("Registration for ApiKey : " + signUp.getApiKey()
                            + " has xsappname : " + " which could not be found in passToken audience(s).");
                }
            }
            else
            {

                throw new ClientBearerException(
                        "Application name (xsappname) not maintained in Service registration or Pass Token Audience is blank");
            }
        }
        return validSignUp;
    }

    private TY_BearerToken getClientBearerToken() throws IOException
    {
        TY_ApplicationDetails acCodeParams;
        TY_BearerToken bearer = null;
        CloseableHttpClient httpClient = null;

        if (StringUtils.hasText(restDestination))
        {

            try
            {
                acCodeParams = CL_DestinationUtilities.getAccessCodeParams4OAuthDestination(restDestination);

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
                                    return null;
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
                log.info("Error accessing Destination  " + restDestination);
                log.info("Error Details :  " + e.getLocalizedMessage());
                return null;
            }
            finally
            {
                if (httpClient != null)
                {
                    httpClient.close();
                }

            }
        }

        return bearer;
    }

}
