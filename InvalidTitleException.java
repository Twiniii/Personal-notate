public class InvalidTitleException extends NoteException
{
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
