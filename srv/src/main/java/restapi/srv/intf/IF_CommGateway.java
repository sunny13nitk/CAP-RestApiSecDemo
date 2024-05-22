package restapi.srv.intf;

import cds.gen.db.userlogs.SrvSignUps;
import restapi.exceptions.ClientBearerException;
import restapi.exceptions.InvalidAPIKeyException;
import restapi.exceptions.JWTTokenException;
import restapi.pojos.TY_CG_CBPL;
import restapi.pojos.TY_CG_CBResponse;
import restapi.pojos.TY_CG_TokenPassInfo;
import restapi.pojos.TY_TokenCheck;

public interface IF_CommGateway
{
  /**
   * Single Point Call from Client Bearer Payload consisting of token and apiKey
   * to return Client Bearer Details
   * 
   * @param cbPayload - Client Bearer Payload consisting of token and apiKey
   * @return - TY_CG_CBResponse :Client Bearer Details
   * @throws ClientBearerException
   */
  public TY_CG_CBResponse getClientBearer(TY_CG_CBPL cbPayload) throws ClientBearerException;

  /**
   * API Key exists and is not expired or Inactive for Consumption
   * 
   * @param apiKey - The Key passed in by the Registered Consumer
   * @return - SrvSignUps - The Srv Sign up Information using apiKey
   * @throws InvalidAPIKeyException
   */
  public SrvSignUps verifyApiKey(String apiKey) throws InvalidAPIKeyException;

  /**
   * Extract Token Information for an Verified Pass Token passed from Registered
   * Consumer App Not to be called diretly before calling verifyPassToken() method
   * 
   * @param passToken - Pass Token passed from Registered Consumer App
   * @return - TY_CG_TokenPassInfo
   * @throws JWTTokenException
   */
  public TY_CG_TokenPassInfo extractTokenInfo(String passToken) throws JWTTokenException;

  /**
   * Validate PassTpken sent by the Consumer for it's Expiration and Signature
   * using PUBLIC Key at runtime
   * 
   * @param - Pass Token passed from Registered Consumer App
   * @return - TY_TokenCheck - complete information on Token Scan
   * @throws JWTTokenException
   */
  public TY_TokenCheck verifyPassToken(String passToken) throws JWTTokenException;

  // @formatter:off
    /**
     * Validate Consumer Pass Token REgistration holistically considering Current Pass Token Information 
     *  - REgistered App 
     *  - Client ID of RegisteredApp
     *  - Scope Checks if enforced on registation
     *  - Check for all scopes present in pass Token scope(s)
     * 
     * @return - true in case Sign Up succesfully Validated
     * @throws ClientBearerException
     */
      // @formatter:on
  public boolean verifyClientSignUp(SrvSignUps signUp, TY_CG_TokenPassInfo passTokenInfo) throws ClientBearerException;

}
