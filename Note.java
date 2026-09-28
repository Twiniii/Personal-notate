import java.util.Objects;

public class Note
{
    String title;
    String text;
    String category;
    int priority;

    public Note(
        String title,
        String text,
        String category,
        int priority
    )
    {
        this.title = title;
        this.text = text;
        this.category = category;
        this.priority = priority;
    }

    @Override
    public String toString()
    {
        return "Назва: " + this.title
            + "\nОпис: " + this.text
            + "\nКатегорія: " + this.category
            + "\nПріоритет: " + this.priority;
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

        return Objects.equals(this.title, other.title)
            && Objects.equals(this.text, other.text)
            && Objects.equals(this.category, other.category)
            && this.priority == other.priority;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(
            title,
            text,
            category,
            priority
        );
    }
}