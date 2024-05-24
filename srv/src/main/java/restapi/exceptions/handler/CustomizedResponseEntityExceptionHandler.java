package restapi.exceptions.handler;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import restapi.exceptions.APISignUpException;
import restapi.exceptions.ClientBearerException;
import restapi.exceptions.InvalidAPIKeyException;

@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler
{
    @ExceptionHandler(APISignUpException.class)
    public final ResponseEntity<ProblemDetail> handleAPIsignUpException(Exception ex, WebRequest request)
            throws Exception
    {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage());
        problemDetail.setTitle("Server Error In APSignUp Process");
        // problemDetail.setType(URI.create("https://todoApp/errors/not-found"));
        problemDetail.setProperty("errorCategory", "Generic");
        problemDetail.setProperty("timestamp", Instant.now());

        return new ResponseEntity<ProblemDetail>(problemDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidAPIKeyException.class)
    public final ResponseEntity<ProblemDetail> handleInvalidAPIKeyException(Exception ex, WebRequest request)
            throws Exception
    {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problemDetail.setTitle("Not Registered for API Usage");
        problemDetail.setType(URI.create("https://helpdocs/apiRegistration"));
        problemDetail.setProperty("errorCategory", "Invalid/ Missing Registration");
        problemDetail.setProperty("timestamp", Instant.now());

        return new ResponseEntity<ProblemDetail>(problemDetail, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ClientBearerException.class)
    public final ResponseEntity<ProblemDetail> handleClientBearerException(Exception ex, WebRequest request)
            throws Exception
    {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problemDetail.setTitle("Exception getting Client Bearer token");
        problemDetail.setType(URI.create("https://helpdocs/clientBearerException"));
        problemDetail.setProperty("errorCategory", "Bearer Generation");
        problemDetail.setProperty("timestamp", Instant.now());

        return new ResponseEntity<ProblemDetail>(problemDetail, HttpStatus.UNAUTHORIZED);
    }
}