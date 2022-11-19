package exception;

public class EntityMissingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntityMissingException(Class<?> cls, Object ref) { super("Entity with reference " + ref + " of " + cls + " not found."); }

}
