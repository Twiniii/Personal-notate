package formatters;

import models.Note;

public class FullNoteFormatter extends AbstractNoteFormatter
{
    public FullNoteFormatter()
    {
        super("Повний", "Назва, текст, категорія та пріоритет окремими рядками.");
    }

    @Override
    public String format(Note note)
    {
        return titleLine(note)
            + "\nТекст: " + note.getText()
            + "\nКатегорія: " + note.getCategory()
            + "\nПріоритет: " + note.getPriority();
    }
}
