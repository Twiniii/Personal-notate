package services;

import java.util.Objects;
import models.ChecklistNote;
import models.Note;
import models.ShoppingListNote;
import models.TextNote;

public final class NoteTypeGuide
{
    private NoteTypeGuide()
    {
    }

    public static String describe(Note note)
    {
        Objects.requireNonNull(note, "Нотатка не може бути null.");
        return switch (note)
        {
            case ShoppingListNote shopping -> "Магазин: "
                + (shopping.getStore().isBlank() ? "не вказано" : shopping.getStore())
                + ". Не придбано позицій: " + shopping.getRemainingCount() + ".";
            case ChecklistNote checklist -> "Невиконаних пунктів: "
                + checklist.getRemainingCount() + ".";
            case TextNote text -> "Джерело: "
                + (text.getSource().isBlank() ? "власний запис" : text.getSource()) + ".";
        };
    }
}
