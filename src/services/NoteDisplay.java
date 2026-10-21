package services;

import java.util.Objects;
import interfaces.NoteFormatter;
import models.Note;

public class NoteDisplay
{
    private NoteFormatter formatter;

    public NoteDisplay(NoteFormatter formatter)
    {
        setFormatter(formatter);
    }

    public void setFormatter(NoteFormatter formatter)
    {
        this.formatter = Objects.requireNonNull(formatter, "Формат не може бути null.");
    }

    public String getFormatName()
    {
        return formatter.getName();
    }

    public void show(Note note)
    {
        formatter.print(note);
    }
}
