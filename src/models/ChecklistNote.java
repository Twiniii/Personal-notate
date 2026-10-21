package models;

import java.util.Arrays;
import diagnostics.ConstructorTrace;
import exceptions.NoteException;

public sealed class ChecklistNote extends Note permits ShoppingListNote
{
    private final String[] items;
    private final boolean[] completed;

    public ChecklistNote(String title, String text, String category, int priority,
        String[] items) throws NoteException
    {
        this(title, text, category, priority, items,
            items == null ? null : new boolean[items.length]);
        ConstructorTrace.log("ChecklistNote: короткий конструктор після this(...)");
    }

    public ChecklistNote(String title, String text, String category, int priority,
        String[] items, boolean[] completed) throws NoteException
    {
        super(title, text, category, priority);
        if (items == null || completed == null || items.length == 0
            || items.length != completed.length)
        {
            throw new NoteException(
                "Список має містити хоча б один пункт і стан для кожного пункту."
            );
        }
        for (String item : items)
        {
            if (item == null || item.isBlank())
            {
                throw new NoteException("Пункт списку не може бути порожнім.");
            }
        }
        this.items = items.clone();
        this.completed = completed.clone();
        ConstructorTrace.log("ChecklistNote: пункти та їхні стани");
    }

    public int getItemCount()
    {
        return items.length;
    }

    public String getItem(int index)
    {
        return items[index];
    }

    public boolean isCompleted(int index)
    {
        return completed[index];
    }

    public int getCompletedCount()
    {
        int count = 0;
        for (boolean value : completed)
        {
            if (value)
            {
                count++;
            }
        }
        return count;
    }

    public int getRemainingCount()
    {
        return getItemCount() - getCompletedCount();
    }

    @Override
    public String getTypeName()
    {
        return "Список справ";
    }

    @Override
    public String getSummary()
    {
        return "Виконано: " + getCompletedCount() + "/" + getItemCount()
            + "; залишилося: " + getRemainingCount();
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder(super.toString());
        result.append("\nПункти:");
        for (int i = 0; i < items.length; i++)
        {
            result.append("\n").append(i + 1).append(". ")
                .append(completed[i] ? "[x] " : "[ ] ").append(items[i]);
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object object)
    {
        if (!super.equals(object))
        {
            return false;
        }
        ChecklistNote other = (ChecklistNote) object;
        return Arrays.equals(items, other.items)
            && Arrays.equals(completed, other.completed);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(items);
        result = 31 * result + Arrays.hashCode(completed);
        return result;
    }
}
