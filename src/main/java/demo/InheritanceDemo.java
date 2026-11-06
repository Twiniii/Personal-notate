package demo;

import diagnostics.ConstructorTrace;
import exceptions.NoteException;
import factories.NoteFormatterFactory;
import models.ChecklistNote;
import models.Note;
import models.ShoppingListNote;
import models.TextNote;
import services.NoteDisplay;
import services.NoteTypeGuide;

public final class InheritanceDemo
{
    private InheritanceDemo()
    {
    }

    public static void run()
    {
        boolean previousTracing = ConstructorTrace.isEnabled();
        try
        {
            System.out.println("=== Ланцюжки конструкторів ===");
            ConstructorTrace.setEnabled(true);
            Note[] examples = {
                new TextNote(1001, "Java", "Повторити наслідування", "Навчання", 2),
                new ChecklistNote(1002, "Справи", "Підготовка до пари", "Навчання", 3,
                    new String[] {"Прочитати README", "Запустити програму"}),
                new ShoppingListNote(1003, "Покупки", "На вечерю", "Особисте", 1,
                    new String[] {"Хліб", "Молоко"}, "Супермаркет")
            };
            ConstructorTrace.setEnabled(false);
            System.out.println("=== Поліморфізм: масив Note[] ===");
            for (Note note : examples)
            {
                System.out.println(note.getTypeName() + ": " + note.getSummary());
                System.out.println(note);
                System.out.println();
            }
            System.out.println("=== Базове посилання й instanceof ===");
            Note reference = examples[2];
            System.out.println("Оголошений тип: Note; фактичний: "
                + reference.getClass().getSimpleName());
            if (reference instanceof ChecklistNote checklist)
            {
                System.out.println("Залишилося пунктів: " + checklist.getRemainingCount());
            }
            System.out.println("=== Sealed і switch за типом ===");
            for (Note note : examples)
            {
                System.out.println(NoteTypeGuide.describe(note));
            }
            System.out.println("=== Нова рівність за ID та окреме порівняння вмісту ===");
            Note sameId = new TextNote(1001, "Java оновлено", "Інший текст", "Навчання", 4);
            Note sameContent = new TextNote(1004, "Java", "Повторити наслідування", "Навчання", 2);
            System.out.println("Той самий ID, змінені дані: " + examples[0].equals(sameId));
            System.out.println("Симетрія: " + sameId.equals(examples[0]));
            System.out.println("Хеші рівних сутностей однакові: "
                + (examples[0].hashCode() == sameId.hashCode()));
            System.out.println("Інший ID, той самий вміст — equals: " + examples[0].equals(sameContent));
            System.out.println("Інший ID, той самий вміст — hasSameContent: "
                + examples[0].hasSameContent(sameContent));
            System.out.println("=== Композиція ===");
            NoteDisplay display = new NoteDisplay(NoteFormatterFactory.create("1"));
            display.show(examples[2]);
            display.setFormatter(NoteFormatterFactory.create("2"));
            display.show(examples[2]);
            System.out.println("NoteDisplay містить форматер, який можна замінити.");
            System.out.println("Демонстраційні об'єкти не додано до вашого блокнота.");
        }
        catch (NoteException error)
        {
            System.out.println("Помилка демонстрації: " + error.getMessage());
        }
        finally
        {
            ConstructorTrace.setEnabled(previousTracing);
        }
    }
}
