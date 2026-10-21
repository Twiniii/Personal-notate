package models;

import java.util.Objects;
import diagnostics.ConstructorTrace;
import exceptions.NoteException;

public final class TextNote extends Note
{
    private final String source;

    public TextNote(String title, String text, String category, int priority)
        throws NoteException
    {
        this(title, text, category, priority, "");
        ConstructorTrace.log("TextNote: короткий конструктор після this(...)");
    }

    public TextNote(String title, String text, String category, int priority, String source)
        throws NoteException
    {
        super(title, text, category, priority);
        this.source = Objects.requireNonNullElse(source, "");
        ConstructorTrace.log("TextNote: джерело");
    }

    public String getSource()
    {
        return source;
    }

    @Override
    public String getTypeName()
    {
        return "Текстова нотатка";
    }

    @Override
    public String getSummary()
    {
        return source.isBlank() ? "Власний запис без указаного джерела."
            : "Джерело запису: " + source;
    }

    @Override
    public String toString()
    {
        return super.toString()
            + "\nДжерело: " + (source.isBlank() ? "не вказано" : source);
    }

    @Override
    public boolean equals(Object object)
    {
        if (!super.equals(object))
        {
            return false;
        }
        TextNote other = (TextNote) object;
        return Objects.equals(source, other.source);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), source);
    }
}
