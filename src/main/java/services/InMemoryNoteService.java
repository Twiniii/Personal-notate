package services;

import java.util.Comparator;
import java.util.List;
import comparators.EntityComparators;
import interfaces.NoteService;
import models.Note;

public class InMemoryNoteService extends InMemoryCrudService<Note> implements NoteService
{
    @Override
    public List<Note> findAllSortedByNameAndByPriority()
    {
        Comparator<Note> comparator = Comparator
            .comparing(Note::getName, EntityComparators.ukrainianNames())
            .thenComparingInt(Note::getPriority);
        return findAllSorted(comparator);
    }
}
