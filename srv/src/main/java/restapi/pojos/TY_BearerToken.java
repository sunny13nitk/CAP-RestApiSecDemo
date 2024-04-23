package restapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TY_BearerToken
{
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
