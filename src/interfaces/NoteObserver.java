package interfaces;

import models.Note;

public interface NoteObserver
{
    void onNoteAdded(Note note, int totalNotes);
}
