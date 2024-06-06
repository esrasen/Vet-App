package simsek.ali.VeterinaryManagementProject.exception;

public class DuplicateDataException extends RuntimeException {
    public DuplicateDataException(Class entityClass) {
        super("Bu '" + entityClass.getSimpleName() + "' zaten kaytlı. Bu isteğin yinelenen verilere neden olur.");
    }
}
