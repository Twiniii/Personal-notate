public class InvalidPriorityException extends NoteException
{
    
    private final int invalidPriority;

    public InvalidPriorityException(String message, int invalidPriority)
    {
        super(message);
        this.invalidPriority = invalidPriority;
    }

    public int getInvalidPriority()
    {
        return invalidPriority;
    }
}
