package adapters;

import java.util.Objects;
import interfaces.Describable;
import interfaces.NoteFormatter;
import legacy.LegacyNoteRenderer;
import models.Note;

public class LegacyNoteFormatterAdapter implements NoteFormatter, Describable
{
    private final LegacyNoteRenderer renderer;

    public LegacyNoteFormatterAdapter(LegacyNoteRenderer renderer)
    {
        this.renderer = Objects.requireNonNull(renderer, "Renderer не може бути null.");
    }

    @Override
    public String getName()
    {
        return "Застарілий";
    }

    @Override
    public String getDescription()
    {
        return "Показ через старий формат запису.";
    }

    @Override
    public String format(Note note)
    {
        return renderer.renderRecord(
            note.getTitle(), note.getText(), note.getCategory(), note.getPriority()
        );
    }
}
