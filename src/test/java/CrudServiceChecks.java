import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import comparators.EntityComparators;
import exceptions.EntityNotFoundException;
import exceptions.InvalidPriorityException;
import exceptions.InvalidTitleException;
import interfaces.CRUDService;
import interfaces.NoteService;
import models.BaseEntity;
import models.Category;
import models.ChecklistNote;
import models.Note;
import models.ShoppingListNote;
import models.TextNote;
import observers.NoteStatisticsObserver;
import services.InMemoryCrudService;
import services.InMemoryNoteService;
import services.Notebook;
import utils.EntityPrinter;

public class CrudServiceChecks
{
    private static int checks;

    public static void main(String[] args) throws Exception
    {
        verifyCrud(new InMemoryCrudService<Note>(),
            new TextNote(1, "Перша", "Початковий текст", "Навчання", 8),
            new ChecklistNote(2, "Друга", "", "", 2, new String[] {"Пункт"}),
            new TextNote(1, "Оновлена", "Новий текст", "Особисте", 1));
        verifyCrud(new InMemoryCrudService<Category>(),
            new Category(11, "Особисте", "Перший опис"),
            new Category(12, "Навчання", "Другий опис"),
            new Category(11, "Змінена", "Новий опис"));
        checkSorting();
        checkEqualityAndValidation();
        checkWildcards();
        checkNotebook();
        System.out.println("Успішних перевірок: " + checks);
    }

    private static <T extends BaseEntity> void verifyCrud(
        CRUDService<T> service, T first, T second, T updated)
    {
        check(service.findAll().isEmpty(), "Нове сховище порожнє");
        check(service.findAllSortedByName().isEmpty(), "Сортування порожнього сховища");
        service.save(first);
        service.save(second);
        check(service.findById(first.getId()) == first, "findById повертає потрібний об'єкт");
        check(service.findAll().size() == 2, "Додавання двох ID");
        service.save(updated);
        check(service.findAll().size() == 2, "Оновлення не додає третій запис");
        check(service.findById(first.getId()) == updated, "Оновлено саме об'єкт за ID");
        check(service.findById(first.getId()).getName().equals(updated.getName()), "Нові поля доступні");
        check(service.findAll().get(0) == updated, "Оновлений ключ зберіг позицію");
        check(service.findAll().get(1) == second, "Інший запис не змінено");
        List<T> copy = service.findAll();
        Collections.reverse(copy);
        copy.clear();
        check(service.findAll().size() == 2 && service.findAll().get(0) == updated,
            "Зміна поверненого списку не змінює сховище");
        expect(NullPointerException.class, () -> service.save(null), "save(null) відхилено");
        expect(NullPointerException.class, () -> service.findAllSorted(null), "null-компаратор відхилено");
        check(service.findAll().size() == 2, "Некоректні виклики не змінили склад");
        check(service.delete(first.getId()), "Видалення наявного ID повертає true");
        check(!service.delete(first.getId()), "Повторне видалення повертає false");
        check(service.findById(second.getId()) == second, "ID іншого запису не змінився");
        EntityNotFoundException error = expect(EntityNotFoundException.class,
            () -> service.findById(first.getId()), "Видалений ID більше не знаходиться");
        check(error.getId() == first.getId(), "Виняток зберігає запитаний ID");
        check(error.getMessage().contains(Long.toString(first.getId())), "Повідомлення містить ID");
    }

    private static void checkSorting() throws Exception
    {
        NoteService service = new InMemoryNoteService();
        Note plans8 = new TextNote(101, "Плани", "", "", 8);
        Note shopping3 = new ShoppingListNote(102, "Покупки", "", "", 3,
            new String[] {"Хліб"}, "Магазин");
        Note plans2 = new ChecklistNote(103, "Плани", "", "", 2, new String[] {"Java"});
        service.save(plans8);
        service.save(shopping3);
        service.save(plans2);
        List<Note> original = service.findAll();
        List<Note> byName = service.findAllSortedByName();
        check(byName.equals(List.of(plans8, plans2, shopping3)), "Порядок за українськими назвами");
        List<Note> byBoth = service.findAllSortedByNameAndByPriority();
        check(byBoth.equals(List.of(plans2, plans8, shopping3)), "Вторинний пріоритет помітний");
        check(byBoth.size() == 3, "Дві однакові назви не втрачено");
        check(service.findAll().equals(original), "Сортування зберегло початковий порядок ID");
        check(service.findAll().get(0) == plans8 && service.findAll().get(1) == shopping3
            && service.findAll().get(2) == plans2, "Сортування не замінило об'єкти");
        byBoth.clear();
        byName.remove(0);
        check(service.findAll().equals(original), "Результати сортування — незалежні списки");
        check(service.findAllSortedByName() != service.findAllSortedByName(), "Кожен виклик створює новий список");
        Comparator<Note> broken = (a, b) -> { throw new IllegalArgumentException("Збій компаратора"); };
        expect(IllegalArgumentException.class, () -> service.findAllSorted(broken), "Збій компаратора передається виклику");
        check(service.findAll().equals(original), "Збій компаратора не пошкодив сховище");
        Comparator<Note> byPriority = Comparator.comparingInt(Note::getPriority);
        check(service.findAllSorted(byPriority).equals(List.of(plans2, shopping3, plans8)), "Довільний компаратор");
        service.save(new TextNote(101, "Абетка", "Оновлено", "", 1));
        check(service.findAllSortedByName().get(0).getId() == 101, "Сортування враховує оновлену назву");
        CRUDService<Category> categories = new InMemoryCrudService<>();
        categories.save(new Category(201, "Дім", ""));
        categories.save(new Category(202, "Ґанок", ""));
        categories.save(new Category(203, "Ганна", ""));
        List<Category> sorted = categories.findAllSortedByName();
        check(sorted.get(0).getName().equals("Ганна") && sorted.get(1).getName().equals("Ґанок")
            && sorted.get(2).getName().equals("Дім"), "Collator: український порядок Г, Ґ, Д");
        categories.save(new Category(204, "Ганна", "Інший запис"));
        check(categories.findAllSortedByName().size() == 4, "Однакові назви категорій не втрачаються");
    }

    private static void checkEqualityAndValidation() throws Exception
    {
        Note first = new TextNote(301, "Назва", "Текст", "Категорія", 1);
        Note updated = new TextNote(301, "Інша назва", "Інший текст", "", 10);
        Note duplicateContent = new TextNote(302, "Назва", "Текст", "Категорія", 1);
        check(first.equals(updated) && updated.equals(first), "Рівність і симетрія за ID");
        check(first.hashCode() == updated.hashCode(), "Хеш не залежить від редагованих даних");
        check(!first.equals(duplicateContent), "Різні ID означають різні сутності");
        check(first.hasSameContent(duplicateContent), "Старий пошук вмісту працює окремо");
        check(!first.hasSameContent(updated), "Оновлений вміст відрізняється");
        check(!first.equals(null) && !first.equals("301"), "Порівняння з null та іншим типом");
        Category sameGlobalId = new Category(301, "Запис з тим самим глобальним ID", "");
        check(first.equals(sameGlobalId) && sameGlobalId.equals(first), "Єдиний простір ID і рівність лише за ID");
        check(EntityComparators.byName().compare(first, duplicateContent) == 0,
            "Компаратор за назвою може дати 0 для різних ID");
        expect(InvalidTitleException.class, () -> new TextNote(303, "   ", "", "", 1), "Порожня назва нотатки");
        expect(InvalidPriorityException.class, () -> new TextNote(303, "Назва", "", "", 20), "Некоректний пріоритет");
        expect(IllegalArgumentException.class, () -> new Category(0, "Назва", ""), "Нульовий ID відхилено");
        expect(IllegalArgumentException.class, () -> new Category(303, null, ""), "null-name відхилено");
        expect(IllegalArgumentException.class, () -> new Category(303, " ", ""), "Порожнє name відхилено");
        String[] items = {"Хліб"};
        boolean[] states = {false};
        ChecklistNote list = new ChecklistNote(304, "Список", "", "", 1, items, states);
        items[0] = "Змінено ззовні";
        states[0] = true;
        check(list.getItem(0).equals("Хліб") && !list.isCompleted(0), "Збережено захисне копіювання масивів");
        CRUDService<Category> largeIds = new InMemoryCrudService<>();
        long largeId = (long) Integer.MAX_VALUE + 100;
        largeIds.save(new Category(largeId, "Великий ID", ""));
        check(largeIds.findById(largeId).getId() == largeId && largeIds.delete(largeId), "ID має тип long");
    }

    private static void checkWildcards() throws Exception
    {
        CRUDService<TextNote> service = new InMemoryCrudService<>();
        service.save(new TextNote(401, "Я", "", "", 1));
        service.save(new TextNote(402, "А", "", "", 2));
        Comparator<BaseEntity> generalComparator = EntityComparators.byName();
        List<TextNote> result = service.findAllSorted(generalComparator);
        check(result.get(0).getId() == 402, "Comparator<BaseEntity> прийнятий для TextNote");
        PrintStream previous = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(bytes, true, StandardCharsets.UTF_8))
        {
            System.setOut(capture);
            EntityPrinter.printAll(result);
            EntityPrinter.printAll(List.of(new Category(403, "Навчання", "")));
        }
        finally
        {
            System.setOut(previous);
        }
        String output = bytes.toString(StandardCharsets.UTF_8);
        check(output.contains("ID: 402") && output.contains("ID: 403"), "printAll приймає різні колекції підтипів");
    }

    private static void checkNotebook() throws Exception
    {
        Notebook notebook = new Notebook(new InMemoryNoteService());
        NoteStatisticsObserver statistics = new NoteStatisticsObserver();
        notebook.addObserver(statistics);
        Note original = new TextNote(501, "Нотатка", "Текст", "", 8);
        notebook.addNote(original);
        List<Note> snapshot = notebook.getNotes();
        expect(IllegalArgumentException.class, () -> notebook.addNote(original), "Повторне UI-додавання того самого ID відхилено");
        notebook.updateNote(new TextNote(501, "Оновлено", "Новий текст", "", 2));
        check(notebook.getNoteCount() == 1 && notebook.getNote(501).getPriority() == 2, "Оновлення через Notebook");
        check(snapshot.get(0) == original && snapshot.get(0).getPriority() == 8, "Старий знімок тримає старий незмінний об'єкт");
        check(statistics.getAddedCount() == 1 && statistics.getCountByPriority(8) == 1,
            "Observer рахує історію створень, не оновлення");
        expect(EntityNotFoundException.class,
            () -> notebook.updateNote(new TextNote(999, "Неіснуюча", "", "", 1)), "Оновлення відсутньої нотатки не створює нової");
        check(notebook.getNoteCount() == 1, "Невдале оновлення не змінило кількість");
        check(notebook.deleteNote(501) && !notebook.deleteNote(501), "Видалення через Notebook");
        check(statistics.getAddedCount() == 1 && notebook.getNoteCount() == 0,
            "Історія додавань і поточна кількість розділені");
    }

    private static void check(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
        checks++;
    }

    private static <E extends Throwable> E expect(Class<E> type, Action action, String message)
    {
        try
        {
            action.run();
        }
        catch (Throwable error)
        {
            if (type.isInstance(error))
            {
                checks++;
                return type.cast(error);
            }
            throw new AssertionError(message + ": інший тип винятку", error);
        }
        throw new AssertionError(message + ": виняток не виник");
    }

    @FunctionalInterface
    private interface Action
    {
        void run() throws Exception;
    }
}
