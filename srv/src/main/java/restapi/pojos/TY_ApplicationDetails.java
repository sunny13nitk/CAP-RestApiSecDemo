package restapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TY_ApplicationDetails
{

    private String authUrl;
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String responseType;
}
