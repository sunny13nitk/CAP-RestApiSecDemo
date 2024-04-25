package restapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TY_TokenRequestBody
{
    private String grant_type;
    private String client_id;
    private String client_secret;
}
