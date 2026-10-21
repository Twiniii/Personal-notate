package exceptions;

public class InvalidTitleException extends NoteException
{
    private static final long serialVersionUID = 1L;

    private final String invalidTitle;

    public InvalidTitleException(String message, String invalidTitle)
    {
        super(message);
        this.invalidTitle = invalidTitle;
    }

    public String getInvalidTitle()
    {
        return invalidTitle;
    }
}
