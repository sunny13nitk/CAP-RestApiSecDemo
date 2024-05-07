package restapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class APISignUpException extends RuntimeException
{
    public APISignUpException(String message)
    {
        super(message);
        log.error(message);
    }
}
