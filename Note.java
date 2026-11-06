import java.util.Objects;

public class Note
{
    
    final String title;
    final String text;
    final String category;
    final int priority;

 
    public Note(String title, String text, String category, int priority)
        throws InvalidTitleException, InvalidPriorityException
    {
        if (title == null || title.isBlank())
        {
            throw new InvalidTitleException(
                "Назва нотатки не може бути порожньою або містити лише пробіли.",
                title
            );
        }

        if (priority < 1 || priority > 10)
        {
            throw new InvalidPriorityException(
                "Пріоритет повинен бути від 1 до 10.", priority
            );
        }

      
        this.title = title;
        this.text = text;
        this.category = category;
        this.priority = priority;
    }

    @Override
    public String toString()
    {
        return "Назва: " + title
            + "\nТекст: " + text
            + "\nКатегорія: " + category
            + "\nПріоритет: " + priority;
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }
        if (object == null || getClass() != object.getClass())
        {
            return false;
        }
        Note other = (Note) object;
        return Objects.equals(title, other.title)
            && Objects.equals(text, other.text)
            && Objects.equals(category, other.category)
            && priority == other.priority;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(title, text, category, priority);
    }
}
