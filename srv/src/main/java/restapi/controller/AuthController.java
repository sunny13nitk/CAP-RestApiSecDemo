package restapi.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
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
    public ResponseEntity<TY_BearerToken> getBearerToken() throws IOException
    {
        TY_BearerToken bearer = null;
        String authCode = null;
        CloseableHttpClient httpClient = null;

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
                if (StringUtils.hasText(url))
                {
                    log.info("URL For Access Code : " + url);
                    HttpResponse response = null;
                    httpClient = HttpClientBuilder.create().build();

                    HttpGet httpGet = new HttpGet(url);

                    // Fire the Url
                    response = httpClient.execute(httpGet);

                    // verify the valid error code first
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY)
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


    @GetMapping("/bearer2")
    public void getToken() throws IOException
    {
        // The client ID and client secret of your application
        String clientId = "sb-java17superapp!t157677";
        String clientSecret = "6AxiQZGgnsAHclyoBM6x2EZ5hmM=";
        // The token endpoint of the authorization server
        String tokenEndpoint = "https://sapit-core-playground-esm.authentication.eu10.hana.ondemand.com/oauth/token";
        // Create an HTTP POST request to the token endpoint
        URL url = new URL(tokenEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        // Set the request headers
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // Write the request body
        PrintWriter writer = new PrintWriter(connection.getOutputStream());
        writer.write("grant_type=client_credentials");
        writer.write("&client_id=" + clientId);
        writer.write("&client_secret=" + clientSecret);
        writer.flush();
        // Get the response
        InputStream responseStream = connection.getInputStream();
        // Read the response body
        byte[] responseBytes = new byte[responseStream.available()];
        responseStream.read(responseBytes);
        // Close the connection
        connection.disconnect();
        // Convert the response bytes to a string
        String responseString = new String(responseBytes);
        // Parse the JSON response
        JSONObject jsonObject = new JSONObject(responseString);
        // Get the access token
        String accessToken = jsonObject.getString("access_token");
        // Print the access token
        log.info("Access token: " + accessToken);
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
