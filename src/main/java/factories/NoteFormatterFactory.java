package factories;

import adapters.LegacyNoteFormatterAdapter;
import formatters.CompactNoteFormatter;
import formatters.FullNoteFormatter;
import interfaces.NoteFormatter;
import legacy.LegacyNoteRenderer;

public final class NoteFormatterFactory
{
    private NoteFormatterFactory()
    {
    }

    public static NoteFormatter create(String code)
    {
        if ("1".equals(code))
        {
            return new FullNoteFormatter();
        }
        if ("2".equals(code))
        {
            return new CompactNoteFormatter();
        }
        if ("3".equals(code))
        {
            return new LegacyNoteFormatterAdapter(new LegacyNoteRenderer());
        }
        throw new IllegalArgumentException("Невідомий код формату: " + code);
    }

    public static NoteFormatter[] createAll()
    {
        return new NoteFormatter[] {
            create("1"),
            create("2"),
            create("3")
        };
    }
}
