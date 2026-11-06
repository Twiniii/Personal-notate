package models;

import exceptions.InvalidTitleException;
import exceptions.InvalidPriorityException;
import diagnostics.ConstructorTrace;
import java.util.Objects;

public abstract sealed class Note extends BaseEntity permits TextNote, ChecklistNote
{

    private final String text;
    private final String category;
    private final int priority;

    protected Note(long id, String title, String text, String category, int priority)
        throws InvalidTitleException, InvalidPriorityException
    {
        super(id, requireTitle(title));

        if (priority < 1 || priority > 10)
        {
            throw new InvalidPriorityException(
                "Пріоритет повинен бути від 1 до 10.", priority
            );
        }

        this.text = text;
        this.category = category;
        this.priority = priority;
        ConstructorTrace.log("Note: спільні поля");
    }

    public String getTitle()
    {
        return getName();
    }

    public String getText()
    {
        return text;
    }

    public String getCategory()
    {
        return category;
    }

    public final int getPriority()
    {
        return priority;
    }

    public abstract String getTypeName();

    public abstract String getSummary();

    @Override
    public String toString()
    {
        return super.toString()
            + "\nТекст: " + text
            + "\nКатегорія: " + category
            + "\nПріоритет: " + priority;
    }

    public boolean hasSameContent(Note other)
    {
        return other != null && getClass() == other.getClass()
            && Objects.equals(getName(), other.getName())
            && Objects.equals(text, other.text)
            && Objects.equals(category, other.category)
            && priority == other.priority;
    }

    private static String requireTitle(String title) throws InvalidTitleException
    {
        if (title == null || title.isBlank())
        {
            throw new InvalidTitleException(
                "Назва нотатки не може бути порожньою або містити лише пробіли.", title
            );
        }
        return title;
    }
}
