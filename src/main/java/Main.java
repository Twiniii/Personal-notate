import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import demo.CrudDemo;
import demo.InheritanceDemo;
import exceptions.EntityNotFoundException;
import exceptions.InvalidPriorityException;
import exceptions.NoteException;
import factories.NoteFormatterFactory;
import interfaces.CRUDService;
import interfaces.Describable;
import interfaces.NoteFormatter;
import models.Category;
import models.ChecklistNote;
import models.Note;
import models.ShoppingListNote;
import models.TextNote;
import observers.ConsoleNoteLogger;
import observers.NoteStatisticsObserver;
import database.DBConnection;
import database.SchemaInitializer;
import database.JdbcIdSequence;
import exceptions.DataAccessException;
import services.jdbc.JdbcCategoryService;
import services.jdbc.JdbcNoteService;
import services.jdbc.CategoryTransferService;
import demo.JdbcDemo;

import services.Notebook;
import services.NoteDisplay;
import services.NoteTypeGuide;
import utils.EntityPrinter;
import utils.IdGenerator;

public class Main
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        try
        {
            var source = DBConnection.getInstance().getDataSource();
            SchemaInitializer.initialize(source);
            IdGenerator ids = new IdGenerator(new JdbcIdSequence(source)::next);
            JdbcNoteService noteService = new JdbcNoteService(source);
            Notebook notebook = new Notebook(noteService);
            CRUDService<Category> categories = new JdbcCategoryService(source);
            CategoryTransferService transfers = new CategoryTransferService(source);
            NoteStatisticsObserver statistics = new NoteStatisticsObserver();
            notebook.addObserver(new ConsoleNoteLogger());
            notebook.addObserver(statistics);
            NoteDisplay display = new NoteDisplay(NoteFormatterFactory.create("1"));

            while (true)
            {
                printMenu(display);
                String choice = scanner.nextLine();
                if (choice.equals("0"))
                {
                    break;
                }
                try
                {
                    if (choice.equals("1"))
                    {
                        addNotes(scanner, notebook, ids);
                    }
                    else if (choice.equals("2"))
                    {
                        printTitles(notebook.getNotes());
                    }
                    else if (choice.equals("3"))
                    {
                        searchByPriority(scanner, notebook, display);
                    }
                    else if (choice.equals("4"))
                    {
                        showSorted(notebook, notebook.findAllSortedByPriority());
                    }
                    else if (choice.equals("5"))
                    {
                        if (notebook.getNoteCount() == 0)
                        {
                            System.out.println("Поки немає нотаток.");
                            continue;
                        }
                        System.out.println("Введіть поля зразка. Він не додається до блокнота.");
                        Note sample = readNote(scanner, ids.next());
                        Note found = notebook.findNoteByContent(sample);
                        if (found == null)
                        {
                            System.out.println("Нотатку з таким вмістом не знайдено.");
                        }
                        else
                        {
                            display.show(found);
                        }
                    }
                    else if (choice.equals("6"))
                    {
                        Note note = selectNote(scanner, notebook);
                        if (note != null)
                        {
                            display.show(note);
                        }
                    }
                    else if (choice.equals("7"))
                    {
                        chooseFormatter(scanner, display);
                    }
                    else if (choice.equals("8"))
                    {
                        Note note = selectNote(scanner, notebook);
                        if (note != null)
                        {
                            for (NoteFormatter formatter : NoteFormatterFactory.createAll())
                            {
                                System.out.println("Формат: " + formatter.getName());
                                formatter.print(note);
                            }
                        }
                    }
                    else if (choice.equals("9"))
                    {
                        printStatistics(statistics, notebook);
                    }
                    else if (choice.equals("10"))
                    {
                        InheritanceDemo.run();
                    }
                    else if (choice.equals("11"))
                    {
                        Note note = selectNote(scanner, notebook);
                        if (note != null)
                        {
                            System.out.println(NoteTypeGuide.describe(note));
                        }
                    }
                    else if (choice.equals("12"))
                    {
                        Note old = selectNote(scanner, notebook);
                        if (old != null)
                        {
                            System.out.println("Введіть усі нові поля. ID залишається " + old.getId() + ".");
                            Note updated = readNote(scanner, old.getId());
                            notebook.updateNote(updated);
                            System.out.println("Нотатку оновлено. Кількість: " + notebook.getNoteCount());
                        }
                    }
                    else if (choice.equals("13"))
                    {
                        printTitles(notebook.getNotes());
                        long id = readId(scanner, "ID для видалення (0 — назад): ");
                        if (id != 0)
                        {
                            System.out.println(notebook.deleteNote(id)
                                ? "Нотатку видалено. Результат delete: true."
                                : "Такої нотатки немає. Результат delete: false.");
                        }
                    }
                    else if (choice.equals("14"))
                    {
                        showSorted(notebook, notebook.findAllSortedByName());
                    }
                    else if (choice.equals("15"))
                    {
                        showSorted(notebook, notebook.findAllSortedByNameAndByPriority());
                    }
                    else if (choice.equals("16"))
                    {
                        CrudDemo.run();
                    }
                    else if (choice.equals("17"))
                    {
                        manageCategories(scanner, categories, ids);
                    }
                    else if (choice.equals("18"))
                    {
                        EntityPrinter.printAll(categories.findAll());
                        long from = readId(scanner, "ID категорії, яку видалити (0 — назад): ");
                        if (from != 0)
                        {
                            long to = readId(scanner, "ID категорії для перенесення (0 — назад): ");
                            if (to != 0)
                            {
                                System.out.println("Перенесено нотаток: " + transfers.moveAndDelete(from, to));
                            }
                        }
                    }
                    else if (choice.equals("19"))
                    {
                        int limit = readInt(scanner, "Кількість на сторінці (1–100): ", 1, 100);
                        int page = readInt(scanner, "Номер сторінки (від 1): ", 1, Integer.MAX_VALUE);
                        printTitles(noteService.findPage(limit, (long) (page - 1) * limit));
                    }
                    else if (choice.equals("20"))
                    {
                        JdbcDemo.run();
                    }
                    else
                    {
                        System.out.println("Такого пункту меню немає.");
                    }
                }
                catch (EntityNotFoundException error)
                {
                    System.out.println(error.getMessage() + " Запитаний ID: " + error.getId());
                }
                catch (InvalidPriorityException error)
                {
                    System.out.println(error.getMessage() + " Введено: " + error.getInvalidPriority());
                    System.out.println("Зміни не збережено.");
                }
                catch (NoteException error)
                {
                    System.out.println(error.getMessage() + " Зміни не збережено.");
                }
                catch (DataAccessException | IllegalArgumentException error)
                {
                    System.out.println(error.getMessage());
                }
            }
        }
        catch (DataAccessException error)
        {
            System.out.println(error.getMessage());
        }
        catch (NoSuchElementException error)
        {
            System.out.println("Введення завершено.");
        }
        finally
        {
            scanner.close();
            System.out.println("До побачення!");
        }
    }

    static void printMenu(NoteDisplay display)
    {
        System.out.println();
        System.out.println("Personal-notate | ЛР №10 — JDBC | Формат: " + display.getFormatName());
        System.out.println("1. Додати нотатки");
        System.out.println("2. Список назв та ID");
        System.out.println("3. Показати та порахувати за пріоритетом");
        System.out.println("4. Показати копію за пріоритетом");
        System.out.println("5. Знайти за повним збігом вмісту");
        System.out.println("6. Відкрити нотатку за ID");
        System.out.println("7. Змінити формат відображення");
        System.out.println("8. Порівняти формати нотатки");
        System.out.println("9. Статистика");
        System.out.println("10. Демонстрація наслідування");
        System.out.println("11. Підказка для типу нотатки");
        System.out.println("12. Оновити нотатку за ID");
        System.out.println("13. Видалити нотатку за ID");
        System.out.println("14. Показати копію за назвою");
        System.out.println("15. Показати копію за назвою та пріоритетом");
        System.out.println("16. Демонстрація CRUD, колекцій і сортування");
        System.out.println("17. Каталог категорій");
        System.out.println("18. Перенести нотатки й видалити стару категорію");
        System.out.println("19. Переглянути назви посторінково");
        System.out.println("20. Перевірка JDBC в окремій тимчасовій БД");
        System.out.println("0. Вийти");
        System.out.print("Оберіть дію: ");
    }

    static void addNotes(Scanner scanner, Notebook notebook, IdGenerator ids)
    {
        int amount = readInt(scanner, "Скільки нотаток додати за раз (1–100): ", 1, 100);
        for (int i = 0; i < amount; i++)
        {
            long id = ids.next();
            boolean created = false;
            while (!created)
            {
                System.out.println("Нова нотатка, ID: " + id);
                try
                {
                    notebook.addNote(readNote(scanner, id));
                    created = true;
                }
                catch (InvalidPriorityException error)
                {
                    System.out.println(error.getMessage() + " Введено: " + error.getInvalidPriority());
                    System.out.println("Введіть нотатку повторно.");
                }
                catch (NoteException error)
                {
                    System.out.println(error.getMessage());
                    System.out.println("Введіть нотатку повторно.");
                }
            }
        }
        System.out.println("Додано нотаток: " + amount);
    }

    static Note readNote(Scanner scanner, long id) throws NoteException
    {
        System.out.println("1. Текстова нотатка");
        System.out.println("2. Список справ");
        System.out.println("3. Список покупок");
        int type = readInt(scanner, "Оберіть тип: ", 1, 3);
        System.out.print("Назва: ");
        String title = scanner.nextLine();
        System.out.print("Текст: ");
        String text = scanner.nextLine();
        System.out.print("Категорія (назва; порожнє поле — Без категорії): ");
        String category = scanner.nextLine();
        if (category.isBlank())
        {
            category = "Без категорії";
        }
        int priority = readInt(scanner, "Пріоритет (1 — найвищий, 10 — найнижчий): ",
            Integer.MIN_VALUE, Integer.MAX_VALUE);
        try
        {
            if (type == 1)
            {
                System.out.print("Джерело (можна пропустити): ");
                return new TextNote(id, title, text, category, priority, scanner.nextLine());
            }
            int count = readInt(scanner, "Кількість пунктів (1–20): ", 1, 20);
            String[] items = new String[count];
            boolean[] completed = new boolean[count];
            for (int i = 0; i < count; i++)
            {
                System.out.print("Пункт №" + (i + 1) + ": ");
                items[i] = scanner.nextLine();
                completed[i] = readInt(scanner, "Виконано / придбано? (1 — так, 0 — ні): ", 0, 1) == 1;
            }
            if (type == 3)
            {
                System.out.print("Магазин (можна пропустити): ");
                return new ShoppingListNote(id, title, text, category, priority,
                    items, completed, scanner.nextLine());
            }
            return new ChecklistNote(id, title, text, category, priority, items, completed);
        }
        catch (NoteException error)
        {
            System.err.println("[Діагностика] Не вдалося створити Note: "
                + error.getClass().getSimpleName());
            throw error;
        }
    }

    static int readInt(Scanner scanner, String message, int min, int max)
    {
        while (true)
        {
            System.out.print(message);
            try
            {
                int value = scanner.nextInt();
                scanner.nextLine();
                if (value >= min && value <= max)
                {
                    return value;
                }
                System.out.printf("Введіть число від %d до %d.%n", min, max);
            }
            catch (InputMismatchException error)
            {
                scanner.nextLine();
                System.out.println("Потрібне ціле число типу int.");
            }
        }
    }

    static long readId(Scanner scanner, String message)
    {
        while (true)
        {
            System.out.print(message);
            try
            {
                long id = Long.parseLong(scanner.nextLine().trim());
                if (id >= 0)
                {
                    return id;
                }
                System.out.println("ID має бути додатним; 0 означає повернення.");
            }
            catch (NumberFormatException error)
            {
                System.out.println("ID має бути цілим числом типу long.");
            }
        }
    }

    static String readName(Scanner scanner, String message)
    {
        while (true)
        {
            System.out.print(message);
            String name = scanner.nextLine();
            if (!name.isBlank())
            {
                return name;
            }
            System.out.println("Назва не може бути порожньою.");
        }
    }

    static void printTitles(List<Note> notes)
    {
        if (notes.isEmpty())
        {
            System.out.println("Поки немає нотаток.");
        }
        for (Note note : notes)
        {
            System.out.printf("ID %d: %s%n", note.getId(), note.getName());
        }
    }

    static Note selectNote(Scanner scanner, Notebook notebook)
    {
        printTitles(notebook.getNotes());
        long id = readId(scanner, "Введіть ID (0 — назад): ");
        return id == 0 ? null : notebook.getNote(id);
    }

    static void searchByPriority(Scanner scanner, Notebook notebook, NoteDisplay display)
    {
        if (notebook.getNoteCount() == 0)
        {
            System.out.println("Поки немає нотаток.");
            return;
        }
        int priority = readInt(scanner, "Пріоритет для пошуку (1–10): ", 1, 10);
        int count = 0;
        for (Note note : notebook.getNotes())
        {
            if (note.getPriority() == priority)
            {
                display.show(note);
                count++;
            }
        }
        System.out.println("Кількість знайдених: " + count);
    }

    static void showSorted(Notebook notebook, List<Note> sorted)
    {
        System.out.println("До сортування: порядок додавання");
        printRows(notebook.getNotes());
        System.out.println("Відсортована копія:");
        printRows(sorted);
        System.out.println("Сховище після сортування:");
        printRows(notebook.getNotes());
    }

    static void printRows(List<Note> notes)
    {
        if (notes.isEmpty())
        {
            System.out.println("Список порожній.");
        }
        for (Note note : notes)
        {
            System.out.printf("ID %d | %s | пріоритет: %d%n",
                note.getId(), note.getName(), note.getPriority());
        }
    }

    static void chooseFormatter(Scanner scanner, NoteDisplay display)
    {
        NoteFormatter[] formatters = NoteFormatterFactory.createAll();
        for (int i = 0; i < formatters.length; i++)
        {
            NoteFormatter formatter = formatters[i];
            System.out.printf("%d. %s", i + 1, formatter.getName());
            if (formatter instanceof Describable describable)
            {
                System.out.print(" — " + describable.getDescription());
            }
            System.out.println();
        }
        int number = readInt(scanner, "Оберіть формат (0 — назад): ", 0, formatters.length);
        if (number != 0)
        {
            display.setFormatter(formatters[number - 1]);
        }
    }

    static void printStatistics(NoteStatisticsObserver statistics, Notebook notebook)
    {
        System.out.println("Зараз у блокноті: " + notebook.getNoteCount());
        System.out.println("Успішно додано за сеанс: " + statistics.getAddedCount());
        System.out.println("Пріоритети на момент додавання (історія, без оновлень і видалень):");
        for (int priority = 1; priority <= 10; priority++)
        {
            System.out.printf("Пріоритет %d: %d%n", priority, statistics.getCountByPriority(priority));
        }
    }

    static void manageCategories(Scanner scanner, CRUDService<Category> categories, IdGenerator ids)
    {
        while (true)
        {
            System.out.println();
            System.out.println("Каталог категорій");
            System.out.println("1. Додати | 2. Переглянути | 3. Знайти за ID");
            System.out.println("4. Оновити | 5. Видалити | 6. За назвою | 0. Назад");
            System.out.print("Дія: ");
            String choice = scanner.nextLine();
            if (choice.equals("0"))
            {
                return;
            }
            try
            {
                if (choice.equals("1"))
                {
                    String name = readName(scanner, "Назва категорії: ");
                    System.out.print("Опис: ");
                    Category category = new Category(ids.next(), name, scanner.nextLine());
                    categories.save(category);
                    System.out.println("Додано категорію з ID " + category.getId());
                }
                else if (choice.equals("2"))
                {
                    EntityPrinter.printAll(categories.findAll());
                }
                else if (choice.equals("3"))
                {
                    long id = readId(scanner, "ID категорії (0 — назад): ");
                    if (id != 0)
                    {
                        System.out.println(categories.findById(id));
                    }
                }
                else if (choice.equals("4"))
                {
                    long id = readId(scanner, "ID для оновлення (0 — назад): ");
                    if (id != 0)
                    {
                        categories.findById(id);
                        String name = readName(scanner, "Нова назва: ");
                        System.out.print("Новий опис: ");
                        categories.save(new Category(id, name, scanner.nextLine()));
                        System.out.println("Категорію оновлено. Кількість: " + categories.findAll().size());
                    }
                }
                else if (choice.equals("5"))
                {
                    long id = readId(scanner, "ID для видалення (0 — назад): ");
                    if (id != 0)
                    {
                        System.out.println("Результат delete: " + categories.delete(id));
                    }
                }
                else if (choice.equals("6"))
                {
                    System.out.println("До сортування:");
                    EntityPrinter.printAll(categories.findAll());
                    System.out.println("Відсортована копія:");
                    EntityPrinter.printAll(categories.findAllSortedByName());
                    System.out.println("Сховище після сортування:");
                    EntityPrinter.printAll(categories.findAll());
                }
                else
                {
                    System.out.println("Такого пункту немає.");
                }
            }
            catch (EntityNotFoundException error)
            {
                System.out.println(error.getMessage());
            }
        }
    }
}
