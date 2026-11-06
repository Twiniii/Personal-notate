package exceptions;

public class EntityNotFoundException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    private final long id;

    public EntityNotFoundException(long id)
    {
        super("Сутності з ID " + id + " не знайдено.");
        this.id = id;
    }

    public long getId()
    {
        return id;
    }
}
