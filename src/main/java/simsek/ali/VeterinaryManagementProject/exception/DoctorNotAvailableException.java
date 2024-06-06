package simsek.ali.VeterinaryManagementProject.exception;

import java.time.LocalDate;

public class DoctorNotAvailableException extends RuntimeException {
    public DoctorNotAvailableException(LocalDate date) {
        super(date + " bu tarihte müsait değil.");
    }
}
