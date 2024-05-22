package restapi.pojos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TY_CG_TokenPassInfo
{
    private String apiKey;
    private List<String> aud;
    private String clientId;
    private List<String> roles;
}
