package formatters;

import interfaces.Describable;
import interfaces.NoteFormatter;
import models.Note;

public abstract class AbstractNoteFormatter implements NoteFormatter, Describable
{
    private final String name;
    private final String description;

    protected AbstractNoteFormatter(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    protected String titleLine(Note note)
    {
        return "Назва: " + note.getTitle();
    }

    @Override
    public abstract String format(Note note);
}
