package demo;

import java.util.Comparator;
import java.util.List;
import comparators.EntityComparators;
import exceptions.EntityNotFoundException;
import exceptions.NoteException;
import interfaces.CRUDService;
import interfaces.NoteService;
import models.BaseEntity;
import models.Category;
import models.ChecklistNote;
import models.Note;
import models.ShoppingListNote;
import models.TextNote;
import services.InMemoryCrudService;
import services.InMemoryNoteService;
import utils.EntityPrinter;

public final class CrudDemo
{
    private CrudDemo()
    {
    }

    public static void main(String[] args) throws NoteException
    {
        run();
    }

    public static void run() throws NoteException
    {
        NoteService notes = new InMemoryNoteService();
        notes.save(new TextNote(101, "Плани", "На тиждень", "Особисте", 8));
        notes.save(new ShoppingListNote(102, "Покупки", "На вечерю", "Особисте", 3,
            new String[] {"Хліб"}, "Супермаркет"));
        notes.save(new ChecklistNote(103, "Плани", "Підготовка до захисту", "Навчання", 2,
            new String[] {"Повторити generics"}));
        List<Note> before = notes.findAll();
        System.out.println("=== До сортування: порядок додавання ===");
        printRows(before);
        System.out.println("=== Копія за назвою ===");
        printRows(notes.findAllSortedByName());
        System.out.println("=== Копія за назвою, потім за пріоритетом ===");
        printRows(notes.findAllSortedByNameAndByPriority());
        System.out.println("=== Сховище після обох сортувань ===");
        printRows(notes.findAll());
        System.out.println("Порядок і склад збережено: " + before.equals(notes.findAll()));
        int countBefore = notes.findAll().size();
        notes.save(new TextNote(101, "Плани", "Оновлений текст", "Особисте", 1));
        System.out.println("=== Оновлення за наявним ID ===");
        System.out.println(notes.findById(101));
        System.out.println("Кількість до / після: " + countBefore + " / " + notes.findAll().size());
        printRows(notes.findAll());
        try
        {
            notes.findById(999);
        }
        catch (EntityNotFoundException error)
        {
            System.out.println("Очікувана помилка: " + error.getMessage());
            System.out.println("ID з винятку: " + error.getId());
        }
        System.out.println("delete(103), існує: " + notes.delete(103));
        System.out.println("delete(103), повторно: " + notes.delete(103));
        System.out.println("=== Той самий generic-клас для двох сутностей ===");
        CRUDService<Note> plainNotes = new InMemoryCrudService<>();
        CRUDService<Category> categories = new InMemoryCrudService<>();
        plainNotes.save(new TextNote(201, "Java", "Повторити колекції", "Навчання", 2));
        categories.save(new Category(202, "Навчання", "Лекції та лабораторні"));
        categories.save(new Category(203, "Особисте", "Побутові записи"));
        categories.save(new Category(202, "Навчання", "Оновлений опис"));
        EntityPrinter.printAll(plainNotes.findAll());
        EntityPrinter.printAll(categories.findAllSortedByName());
        System.out.println("Категорій після оновлення: " + categories.findAll().size());
        System.out.println("Категорія delete(203): " + categories.delete(203));
        System.out.println("Категорія delete(203) повторно: " + categories.delete(203));
        System.out.println("=== Comparator<? super T> і Collection<? extends BaseEntity> ===");
        CRUDService<TextNote> textNotes = new InMemoryCrudService<>();
        textNotes.save(new TextNote(301, "Яблука", "", "", 1));
        textNotes.save(new TextNote(302, "Абетка", "", "", 2));
        Comparator<BaseEntity> baseComparator = EntityComparators.byName();
        List<TextNote> result = textNotes.findAllSorted(baseComparator);
        EntityPrinter.printAll(result);
        System.out.println("=== Рівність за ID та порядок за назвою ===");
        Note first = new TextNote(401, "Одна назва", "", "", 1);
        Note second = new TextNote(402, "Одна назва", "", "", 1);
        System.out.println("Різні ID — equals: " + first.equals(second));
        System.out.println("Однакові назви — compare: " + baseComparator.compare(first, second));
        System.out.println("Демонстрація працювала з окремими сервісами. Ваші дані не змінено.");
    }

    private static void printRows(List<Note> notes)
    {
        for (Note note : notes)
        {
            System.out.printf("ID: %d | %s | пріоритет: %d%n",
                note.getId(), note.getName(), note.getPriority());
        }
    }
}
