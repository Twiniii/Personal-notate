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
                new TextNote("Java", "Повторити наслідування", "Навчання", 2),
                new ChecklistNote("Справи", "Підготовка до пари", "Навчання", 3,
                    new String[] {"Прочитати README", "Запустити програму"}),
                new ShoppingListNote("Покупки", "На вечерю", "Особисте", 1,
                    new String[] {"Хліб", "Молоко"}, "Супермаркет")
            };
            ConstructorTrace.setEnabled(false);

            System.out.println("=== Поліморфізм: масив Note[] ===");
            for (Note note : examples)
            {
                System.out.println(note.getTypeName() + ": " + note.getSummary());
                System.out.println(note.toString());
                System.out.println();
            }

            System.out.println("=== Базове посилання й instanceof ===");
            Note reference = examples[2];
            System.out.println("Оголошений тип reference: Note");
            System.out.println("Фактичний тип: " + reference.getClass().getSimpleName());
            if (reference instanceof ChecklistNote checklist)
            {
                System.out.println("Специфічний метод getRemainingCount(): "
                    + checklist.getRemainingCount());
            }

            System.out.println("=== Sealed і switch за типом ===");
            for (Note note : examples)
            {
                System.out.println(NoteTypeGuide.describe(note));
            }

            System.out.println("=== Симетрія equals() ===");
            ChecklistNote base = new ChecklistNote("Список", "", "Особисте", 2,
                new String[] {"Хліб"});
            ShoppingListNote sub = new ShoppingListNote("Список", "", "Особисте", 2,
                new String[] {"Хліб"}, "Магазин А");
            ShoppingListNote same = new ShoppingListNote("Список", "", "Особисте", 2,
                new String[] {"Хліб"}, "Магазин А");
            ShoppingListNote otherStore = new ShoppingListNote("Список", "", "Особисте", 2,
                new String[] {"Хліб"}, "Магазин Б");

            System.out.println("ChecklistNote.equals(ShoppingListNote): " + base.equals(sub));
            System.out.println("ShoppingListNote.equals(ChecklistNote): " + sub.equals(base));
            System.out.println("Однаковий тип і всі поля: " + sub.equals(same));
            System.out.println("Їхні hashCode однакові: " + (sub.hashCode() == same.hashCode()));
            System.out.println("Інший магазин: " + sub.equals(otherStore));
            Note baseReference = sub;
            System.out.println("Той самий об'єкт через Note: "
                + (baseReference.equals(sub) && sub.equals(baseReference)));

            System.out.println("=== Композиція з ЛР №4–5 ===");
            NoteDisplay display = new NoteDisplay(NoteFormatterFactory.create("1"));
            display.show(examples[2]);
            display.setFormatter(NoteFormatterFactory.create("2"));
            display.show(examples[2]);
            System.out.println("NoteDisplay має форматер; він не є форматером або нотаткою.");
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
