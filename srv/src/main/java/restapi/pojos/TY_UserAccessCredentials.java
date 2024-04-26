package restapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TY_UserAccessCredentials
{
    public String username;
    public String password;
    // public String token; This should be passed in Called REquest header tokenPass
}
