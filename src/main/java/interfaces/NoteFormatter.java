package interfaces;

import models.Note;

public interface NoteFormatter
{
    String getName();

    String format(Note note);

    default void print(Note note)
    {
        System.out.println(format(note));
    }
}
