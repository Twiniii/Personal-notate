package models;

import java.util.Objects;
import diagnostics.ConstructorTrace;
import exceptions.NoteException;

public final class ShoppingListNote extends ChecklistNote
{
    private final String store;

    public ShoppingListNote(long id, String title, String text, String category, int priority,
        String[] items, String store) throws NoteException
    {
        this(id, title, text, category, priority, items,
            items == null ? null : new boolean[items.length], store);
        ConstructorTrace.log("ShoppingListNote: короткий конструктор після this(...)");
    }

    public ShoppingListNote(long id, String title, String text, String category, int priority,
        String[] items, boolean[] completed, String store) throws NoteException
    {
        super(id, title, text, category, priority, items, completed);
        this.store = Objects.requireNonNullElse(store, "");
        ConstructorTrace.log("ShoppingListNote: магазин");
    }

    public String getStore()
    {
        return store;
    }

    @Override
    public String getTypeName()
    {
        return "Список покупок";
    }

    @Override
    public String getSummary()
    {
        return "Залишилося купити: " + getRemainingCount()
            + "; магазин: " + (store.isBlank() ? "не вказано" : store);
    }

    @Override
    public String toString()
    {
        return super.toString()
            + "\nМагазин: " + (store.isBlank() ? "не вказано" : store);
    }

    @Override
    public boolean hasSameContent(Note other)
    {
        if (!super.hasSameContent(other))
        {
            return false;
        }
        ShoppingListNote note = (ShoppingListNote) other;
        return Objects.equals(store, note.store);
    }
}
