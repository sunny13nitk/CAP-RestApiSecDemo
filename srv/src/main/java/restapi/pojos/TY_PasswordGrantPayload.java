package restapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TY_PasswordGrantPayload
{
    private String grant_type;
    private String client_id;
    private String client_secret;
    private String username;
    private String password;
}
