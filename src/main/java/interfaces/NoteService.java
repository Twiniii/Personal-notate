package interfaces;

import java.util.List;
import models.Note;

public interface NoteService extends CRUDService<Note>
{
    List<Note> findAllSortedByNameAndByPriority();
}
