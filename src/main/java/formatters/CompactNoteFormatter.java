package formatters;

import models.Note;

public class CompactNoteFormatter extends AbstractNoteFormatter
{
    public CompactNoteFormatter()
    {
        super("Стислий", "Назва, категорія та пріоритет одним рядком, без тексту.");
    }

    @Override
    public String format(Note note)
    {
        return titleLine(note)
            + " | Категорія: " + note.getCategory()
            + " | Пріоритет: " + note.getPriority();
    }
}
