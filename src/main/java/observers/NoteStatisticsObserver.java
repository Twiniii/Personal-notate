package observers;

import interfaces.NoteObserver;
import models.Note;

public class NoteStatisticsObserver implements NoteObserver
{
    private int addedCount;
    private final int[] countsByPriority = new int[11];

    @Override
    public void onNoteAdded(Note note, int totalNotes)
    {
        addedCount++;
        countsByPriority[note.getPriority()]++;
    }

    public int getAddedCount()
    {
        return addedCount;
    }

    public int getCountByPriority(int priority)
    {
        if (priority < 1 || priority > 10)
        {
            throw new IllegalArgumentException("Пріоритет повинен бути від 1 до 10.");
        }
        return countsByPriority[priority];
    }
}
