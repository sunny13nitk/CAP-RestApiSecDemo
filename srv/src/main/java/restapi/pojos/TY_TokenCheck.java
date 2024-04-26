package restapi.pojos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TY_TokenCheck
{
    private boolean validSignature;
    private boolean expired;
    private String userId;
    private String email;
    private List<String> scopes;
    private long exp;
    private String message;
}
