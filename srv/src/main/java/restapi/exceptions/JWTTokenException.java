package restapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class JWTTokenException extends RuntimeException
{
    public JWTTokenException(String message)
    {

        super(message);
        log.error(message);
    }
}
