package restapi.utilities;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import restapi.exceptions.JWTTokenException;

@Slf4j
public class JWTTokenUtilities
{
    public static final String gc_algoName = "RSA";

    public static RSAPublicKey getRSAPublicKey(String header) throws JWTTokenException, IOException
    {
        RSAPublicKey rsaKey = null;
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        JsonNode jsonNode = null;
        String publicKey = null;

        if (StringUtils.hasText(header))
        {
            // Check Header Structure
            // Parse the decoded header as a JSON object.
            JSONObject jsonHeader = new JSONObject(new String(header));
            if (jsonHeader != null)
            {
                // Get the value of the "alg" property.
                String algorithm = jsonHeader.getString("alg");
                if (!StringUtils.hasText(algorithm))
                {
                    throw new JWTTokenException("Invalid Token Header Structure for tokenPass");
                }
            }
            // Get Token(s) Url
            String tokenKeyUrl = header.split(",")[1].split(":")[1];
            if (StringUtils.hasText(tokenKeyUrl))
            {
                try
                {

                    httpClient = HttpClientBuilder.create().build();

                    HttpGet httpGet = new HttpGet(tokenKeyUrl);
                    if (httpGet != null)
                    {
                        // Fire the Url

                        response = httpClient.execute(httpGet);

                        int statusCode = response.getStatusLine().getStatusCode();
                        if (statusCode != org.apache.http.HttpStatus.SC_OK)
                        {
                            throw new JWTTokenException("Failed with HTTP error code : " + statusCode
                                    + "while retreiving Token verification Public Key");
                        }
                        else
                        {
                            log.info(
                                    "Authentication Successful for retreiving Token verification Public Key for tokenPass");

                            // Try and Get Entity from Response
                            HttpEntity entity = response.getEntity();
                            String apiOutput = EntityUtils.toString(entity);
                            // Lets see what we got from API
                            // Log.info(apiOutput);

                            // Conerting to JSON
                            ObjectMapper mapper = new ObjectMapper();
                            jsonNode = mapper.readTree(apiOutput);

                            if (jsonNode != null)
                            {
                                JsonNode contentNode = jsonNode.at("/keys");
                                if (contentNode != null && contentNode.isArray() && contentNode.size() > 0)
                                {
                                    for (JsonNode arrayItem : contentNode)
                                    {
                                        Iterator<Entry<String, JsonNode>> fields = arrayItem.fields();
                                        while (fields.hasNext())
                                        {
                                            Entry<String, JsonNode> jsonField = fields.next();
                                            if (jsonField.getKey().equals("value"))
                                            {
                                                publicKey = jsonField.getValue().asText();
                                                if (StringUtils.hasText(publicKey))
                                                {
                                                    String publicKeyPEM = publicKey
                                                            .replace("-----BEGIN PUBLIC KEY-----", "");
                                                    publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
                                                    publicKeyPEM = publicKeyPEM.replace("\n", "");

                                                    // Decode the contents of the file from Base64
                                                    byte[] base64EncodedKeyBytes = java.util.Base64.getDecoder()
                                                            .decode(publicKeyPEM);

                                                    // Convert the contents of the file to a RSAPublicKey object
                                                    X509EncodedKeySpec spec = new X509EncodedKeySpec(
                                                            base64EncodedKeyBytes);

                                                    KeyFactory kf = KeyFactory.getInstance(gc_algoName);
                                                    rsaKey = (RSAPublicKey) kf.generatePublic(spec);
                                                    return rsaKey;
                                                }

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
                    throw new JWTTokenException(
                            "Error Fetching Public Verification key for passToken. Details -  " + e.getMessage());
                }

                finally
                {
                    response.close();
                    httpClient.close();
                }

            }
            else
            {
                throw new JWTTokenException(
                        "No path specified in tokenPass Header to retrive token signature verification Key(s)");
            }

        }

        return rsaKey;
    }
}
