package simsek.ali.VeterinaryManagementProject.exception;

public class EntityAlreadyExistException extends RuntimeException {
    public EntityAlreadyExistException(Class entityClass) {
        super("'" + entityClass.getSimpleName() + "' zaten kaydedildi.");
    }
}
