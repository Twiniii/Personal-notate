package observers;

import interfaces.NoteObserver;
import models.Note;

public class ConsoleNoteLogger implements NoteObserver
{
    @Override
    public void onNoteAdded(Note note, int totalNotes)
    {
        System.out.println("[Журнал] Додано нотатку «" + note.getTitle()
            + "». Усього: " + totalNotes);
    }
}
