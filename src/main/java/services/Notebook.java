package services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import interfaces.NoteObserver;
import interfaces.NoteService;
import models.Note;

public class Notebook
{
    private final NoteService service;
    private final List<NoteObserver> observers = new ArrayList<>();

    public Notebook(NoteService service)
    {
        this.service = Objects.requireNonNull(service, "Сервіс не може бути null.");
    }

    public int getNoteCount()
    {
        return service.findAll().size();
    }

    public List<Note> getNotes()
    {
        return service.findAll();
    }

    public Note getNote(long id)
    {
        return service.findById(id);
    }

    public void addNote(Note note)
    {
        Objects.requireNonNull(note, "Нотатка не може бути null.");
        for (Note existing : service.findAll())
        {
            if (existing.getId() == note.getId())
            {
                throw new IllegalArgumentException("Цей ID уже зайнятий. Скористайтеся оновленням.");
            }
        }
        service.save(note);
        notifyObservers(note);
    }

    public void updateNote(Note note)
    {
        Objects.requireNonNull(note, "Нотатка не може бути null.");
        service.findById(note.getId());
        service.save(note);
    }

    public boolean deleteNote(long id)
    {
        return service.delete(id);
    }

    public List<Note> findAllSortedByPriority()
    {
        return service.findAllSorted(Comparator.comparingInt(Note::getPriority));
    }

    public List<Note> findAllSortedByName()
    {
        return service.findAllSortedByName();
    }

    public List<Note> findAllSortedByNameAndByPriority()
    {
        return service.findAllSortedByNameAndByPriority();
    }

    public Note findNoteByContent(Note sample)
    {
        for (Note note : service.findAll())
        {
            if (note.hasSameContent(sample))
            {
                return note;
            }
        }
        return null;
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
                observer.onNoteAdded(note, getNoteCount());
            }
            catch (RuntimeException error)
            {
                System.err.println("Помилка сповіщення "
                    + observer.getClass().getSimpleName() + ": " + error.getMessage());
            }
        }
    }
}
