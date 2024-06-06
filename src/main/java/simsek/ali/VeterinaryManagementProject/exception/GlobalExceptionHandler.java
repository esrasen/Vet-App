package simsek.ali.VeterinaryManagementProject.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFoundExceptionHandler(EntityNotFoundException exception, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(generateErrorResponse(404, exception, request));
    }

    @ExceptionHandler(EntityAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> entityAlreadyExistHandler(EntityAlreadyExistException exception, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(generateErrorResponse(400, exception, request));
    }

    @ExceptionHandler(DuplicateDataException.class)
    public ResponseEntity<ErrorResponse> duplicateDataExceptionHandler(DuplicateDataException exception, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(generateErrorResponse(400, exception, request));
    }

    @ExceptionHandler(DoctorAppointmentConflictException.class)
    public ResponseEntity<ErrorResponse> doctorAppointmentConflictExceptionHandler(DoctorAppointmentConflictException exception, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(generateErrorResponse(400, exception, request));
    }

    @ExceptionHandler(DoctorNotAvailableException.class)
    public ResponseEntity<ErrorResponse> doctorNotAvailableExceptionHandler(DoctorNotAvailableException exception, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(generateErrorResponse(400, exception, request));
    }

    @ExceptionHandler(ProtectionStillActiveException.class)
    public ResponseEntity<ErrorResponse> protectionStillActiveExceptionHandler(ProtectionStillActiveException exception, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(generateErrorResponse(400, exception, request));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception, HttpServletRequest request) {
        String paramName = exception.getParameterName();
        String message = String.format("Gerekli istek parametresi '%s' eksik veya geçersiz.", paramName);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(generateErrorResponse(400, message, request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException exception, HttpServletRequest request) {
        StringBuilder message = new StringBuilder("Validation failed for: ");
        exception.getBindingResult().getFieldErrors().forEach(error -> message.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(generateErrorResponse(400, new RuntimeException(message.toString()), request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(generateErrorResponse(500, exception, request));
    }

    public ErrorResponse generateErrorResponse(int status, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(status);
        errorResponse.setMessage(message);
        errorResponse.setPath(request.getRequestURI());
        return errorResponse;
    }

    public ErrorResponse generateErrorResponse(int status, Exception ex, HttpServletRequest request) {
        return generateErrorResponse(status, ex.getMessage(), request);
    }
}
