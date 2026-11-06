package formatters;

import models.Note;

public class FullNoteFormatter extends AbstractNoteFormatter
{
    public FullNoteFormatter()
    {
        super("Повний", "Усі спільні й додаткові поля нотатки окремими рядками.");
    }

    @Override
    public String format(Note note)
    {
        return "Тип: " + note.getTypeName() + "\n" + note.toString();
    }
}
