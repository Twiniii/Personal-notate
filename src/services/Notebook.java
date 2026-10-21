package services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import interfaces.NoteObserver;
import models.Note;

public class Notebook
{
    private final Note[] notes;
    private int noteCount;
    private final List<NoteObserver> observers = new ArrayList<>();

    public Notebook(int capacity)
    {
        if (capacity < 1)
        {
            throw new IllegalArgumentException("Місткість має бути додатною.");
        }
        notes = new Note[capacity];
    }

    public int getNoteCount()
    {
        return noteCount;
    }

    public int getFreePlaces()
    {
        return notes.length - noteCount;
    }

    public Note[] getNotes()
    {
        return Arrays.copyOf(notes, noteCount);
    }

    public Note getNote(int number)
    {
        if (number < 1 || number > noteCount)
        {
            throw new IndexOutOfBoundsException(
                "Нотатки з номером " + number
                + " немає. Допустимі номери: 1–" + noteCount + "."
            );
        }
        return notes[number - 1];
    }

    public void addNote(Note note)
    {
        Objects.requireNonNull(note, "Нотатка не може бути null.");
        if (noteCount >= notes.length)
        {
            throw new IllegalStateException("Масив нотаток заповнений.");
        }

        notes[noteCount] = note;
        noteCount++;
        notifyObservers(note);
    }

    public void addObserver(NoteObserver observer)
    {
        Objects.requireNonNull(observer, "Спостерігач не може бути null.");
        if (!observers.contains(observer))
        {
            observers.add(observer);
        }
    }

    public void removeObserver(NoteObserver observer)
    {
        observers.remove(observer);
    }

    private void notifyObservers(Note note)
    {
        for (NoteObserver observer : new ArrayList<>(observers))
        {
            try
            {
                observer.onNoteAdded(note, noteCount);
            }
            catch (RuntimeException error)
            {
                System.err.println("Помилка сповіщення "
                    + observer.getClass().getSimpleName() + ": " + error.getMessage());
            }
        }
    }

    public void sortByPriority()
    {
        for (int i = 0; i < noteCount - 1; i++)
        {
            for (int j = 0; j < noteCount - 1 - i; j++)
            {
                if (notes[j].getPriority() > notes[j + 1].getPriority())
                {
                    Note temporary = notes[j];
                    notes[j] = notes[j + 1];
                    notes[j + 1] = temporary;
                }
            }
        }
    }

    public int findNote(Note sample)
    {
        for (int i = 0; i < noteCount; i++)
        {
            if (notes[i].equals(sample))
            {
                return i;
            }
        }
        return -1;
    }
}
