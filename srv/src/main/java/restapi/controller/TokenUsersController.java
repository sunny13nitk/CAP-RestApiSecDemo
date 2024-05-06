package restapi.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import restapi.pojos.TY_ApplicationDetails;
import restapi.pojos.TY_BearerToken;
import restapi.pojos.TY_TokenRequestBody;
import restapi.pojos.TY_UserAccessCredentials;
import restapi.utilities.CL_DestinationUtilities;

@RestController
@RequestMapping("/token-user")
@Slf4j
@RequiredArgsConstructor
public class TokenUsersController
{
    private final String desNameOAuthCred = "REST_API_BEARER";

    @PostMapping("/")
    public ResponseEntity<TY_BearerToken> getToken4User(@RequestBody TY_UserAccessCredentials userAccessCredentials)
            throws IOException
    {
        log.info("Inside Token retrival POST call......");
        TY_ApplicationDetails acCodeParams;
        TY_BearerToken bearer = null;
        CloseableHttpClient httpClient = null;

        if (userAccessCredentials != null)
        {

            if (StringUtils.hasText(userAccessCredentials.getUsername())
                    && StringUtils.hasText(userAccessCredentials.getPassword()))
            {
                try
                {
                    acCodeParams = CL_DestinationUtilities.getAccessCodeParams4OAuthDestination(desNameOAuthCred);

                    if (acCodeParams != null)
                    {
                        log.info("Destination Access successful.");
                        // Create an HTTP POST request to the token endpoint
                        String url = acCodeParams.getAuthUrl();

                        if (StringUtils.hasText(url) && StringUtils.hasText(acCodeParams.getClientId())
                                && StringUtils.hasText(acCodeParams.getClientSecret()))
                        {

                            TY_TokenRequestBody reqBody = new TY_TokenRequestBody(CL_DestinationUtilities.GC_Password,
                                    acCodeParams.getClientId(), acCodeParams.getClientSecret());
                            if (reqBody != null)
                            {

                                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                                formparams.add(new BasicNameValuePair(CL_DestinationUtilities.GC_GrantType,
                                        reqBody.getGrant_type()));
                                formparams.add(new BasicNameValuePair(CL_DestinationUtilities.GC_ClientID_Token,
                                        reqBody.getClient_id()));
                                formparams.add(new BasicNameValuePair(CL_DestinationUtilities.GC_ClientSecret_Token,
                                        reqBody.getClient_secret()));

                                formparams.add(new BasicNameValuePair(CL_DestinationUtilities.GC_Username,
                                        userAccessCredentials.getUsername()));

                                formparams.add(new BasicNameValuePair(CL_DestinationUtilities.GC_Password,
                                        userAccessCredentials.getPassword()));

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
                                    log.info("Post Payload Details");
                                    log.info(entity.toString());

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
                                        log.info("Bearer token call result " + statusCode);
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

}
