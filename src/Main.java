import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

import exceptions.InvalidPriorityException;
import exceptions.NoteException;
import factories.NoteFormatterFactory;
import observers.ConsoleNoteLogger;
import observers.NoteStatisticsObserver;
import services.Notebook;
import interfaces.Describable;
import interfaces.NoteFormatter;
import models.Note;
import services.NoteDisplay;

public class Main
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        try
        {
            Notebook notebook = new Notebook(100);
            NoteStatisticsObserver statistics = new NoteStatisticsObserver();
            notebook.addObserver(new ConsoleNoteLogger());
            notebook.addObserver(statistics);
            NoteDisplay display = new NoteDisplay(NoteFormatterFactory.create("1"));
            System.out.println("   Personal - Notate   ");

            while (true)
            {
                System.out.println();
                System.out.println("Формат: " + display.getFormatName());
                System.out.println("1. Додати нотатки");
                System.out.println("2. Переглянути нотатки");
                System.out.println("3. Показати та порахувати за пріоритетом");
                System.out.println("4. Відсортувати за пріоритетом");
                System.out.println("5. Знайти нотатку за повним збігом");
                System.out.println("6. Відкрити нотатку за номером");
                System.out.println("7. Змінити формат відображення");
                System.out.println("8. Порівняти формати однієї нотатки");
                System.out.println("9. Статистика доданих нотаток");
                System.out.println("0. Вийти");
                System.out.print("Оберіть дію: ");
                String choice = scanner.nextLine();

                if (choice.equals("1"))
                {
                    if (notebook.getFreePlaces() == 0)
                    {
                        System.out.println("Масив нотаток заповнений.");
                        continue;
                    }
                    int freePlaces = notebook.getFreePlaces();
                    int amount = readInt(scanner,
                        "Скільки нотаток додати (1–" + freePlaces + "): ",
                        1, freePlaces);

                    for (int i = 0; i < amount; i++)
                    {
                        boolean created = false;

                        while (!created)
                        {
                            System.out.println("Нотатка №" + (notebook.getNoteCount() + 1));
                            try
                            {
                                Note note = readNote(scanner);
                                notebook.addNote(note);
                                created = true;
                            }
                            catch (InvalidPriorityException error)
                            {
                                System.out.println(error.getMessage());
                                System.out.println("Введений пріоритет: "
                                    + error.getInvalidPriority());
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
                else if (choice.equals("2"))
                {
                    printNotes(notebook.getNotes(), display);
                }
                else if (choice.equals("3"))
                {
                    if (notebook.getNoteCount() == 0)
                    {
                        System.out.println("Поки немає створених нотаток.");
                        continue;
                    }
                    int priority = readInt(scanner,
                        "Введіть пріоритет для пошуку (1–10): ", 1, 10);
                    int foundCount = 0;
                    Note[] notes = notebook.getNotes();
                    for (int i = 0; i < notebook.getNoteCount(); i++)
                    {
                        if (notes[i].getPriority() == priority)
                        {
                            System.out.println("Нотатка №" + (i + 1));
                            display.show(notes[i]);
                            foundCount++;
                        }
                    }
                    if (foundCount == 0)
                    {
                        System.out.println("Нотаток із таким пріоритетом не знайдено.");
                    }
                    System.out.println("Кількість знайдених: " + foundCount);
                }
                else if (choice.equals("4"))
                {
                    if (notebook.getNoteCount() == 0)
                    {
                        System.out.println("Поки немає створених нотаток.");
                        continue;
                    }
                    System.out.println("До сортування:");
                    printNotes(notebook.getNotes(), display);
                    notebook.sortByPriority();
                    System.out.println("Після сортування:");
                    printNotes(notebook.getNotes(), display);
                }
                else if (choice.equals("5"))
                {
                    if (notebook.getNoteCount() == 0)
                    {
                        System.out.println("Поки немає створених нотаток.");
                        continue;
                    }
                    try
                    {
                        System.out.println("Введіть усі поля нотатки-зразка.");
                        Note sample = readNote(scanner);
                        int index = notebook.findNote(sample);
                        if (index == -1)
                        {
                            System.out.println("Нотатку з повним збігом не знайдено.");
                        }
                        else
                        {
                            System.out.println("Знайдено нотатку №" + (index + 1));
                            display.show(notebook.getNote(index + 1));
                        }
                    }
                    catch (NoteException error)
                    {
                        System.out.println("Пошук скасовано: " + error.getMessage());
                    }
                }
                else if (choice.equals("6"))
                {
                    openNote(scanner, notebook, display);
                }
                else if (choice.equals("7"))
                {
                    chooseFormatter(scanner, display);
                }
                else if (choice.equals("8"))
                {
                    showFormatExamples(scanner, notebook.getNotes(), notebook.getNoteCount());
                }
                else if (choice.equals("9"))
                {
                    printStatistics(statistics);
                }
                else if (choice.equals("0"))
                {
                    break;
                }
                else
                {
                    System.out.println("Такого варіанту немає, спробуйте ще раз.");
                }
            }
        }
        catch (NoSuchElementException error)
        {
            System.out.println("Введення завершено. Програма завершує роботу.");
        }
        finally
        {
            scanner.close();
            System.out.println("До побачення!");
        }
    }

    static Note readNote(Scanner scanner) throws NoteException
    {
        System.out.print("Введіть назву нотатки: ");
        String title = scanner.nextLine();
        System.out.print("Введіть текст нотатки: ");
        String text = scanner.nextLine();
        System.out.print("Введіть категорію нотатки: ");
        String category = scanner.nextLine();

        int priority = readInt(scanner,
            "Введіть пріоритет (1 — найвищий, 10 — найнижчий): ",
            Integer.MIN_VALUE, Integer.MAX_VALUE);
        try
        {
            return new Note(title, text, category, priority);
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
                System.out.println("Помилка: потрібно ввести ціле число типу int.");
            }
        }
    }

    static void openNote(Scanner scanner, Notebook notebook, NoteDisplay display)
    {
        Note[] notes = notebook.getNotes();
        int noteCount = notebook.getNoteCount();
        if (noteCount == 0)
        {
            System.out.println("Поки немає створених нотаток.");
            return;
        }

        for (int i = 0; i < noteCount; i++)
        {
            System.out.printf("%d. %s%n", i + 1, notes[i].getTitle());
        }
        System.out.print("Введіть номер нотатки (0 — назад): ");
        try
        {
            int number = Integer.parseInt(scanner.nextLine().trim());
            if (number == 0)
            {
                return;
            }
            display.show(notebook.getNote(number));
        }
        catch (NumberFormatException error)
        {
            System.out.println("Номер має бути цілим числом типу int. Повернення до меню.");
        }
        catch (IndexOutOfBoundsException error)
        {
            System.out.println(error.getMessage());
        }
    }

    static void printNotes(Note[] notes, NoteDisplay display)
    {
        int number = 1;
        for (Note note : notes)
        {
            if (note == null)
            {
                continue;
            }
            System.out.println();
            System.out.println("Нотатка №" + number);
            display.show(note);
            number++;
        }
        if (number == 1)
        {
            System.out.println("Поки немає створених нотаток.");
        }
    }

    static void chooseFormatter(Scanner scanner, NoteDisplay display)
    {
        NoteFormatter[] formatters = NoteFormatterFactory.createAll();

        for (int i = 0; i < formatters.length; i++)
        {
            NoteFormatter formatter = formatters[i];
            System.out.printf("%d. %s", i + 1, formatter.getName());
            if (formatter instanceof Describable)
            {
                Describable description = (Describable) formatter;
                System.out.print(" — " + description.getDescription());
            }
            System.out.println();
        }

        int number = readInt(scanner, "Оберіть формат (0 — назад): ", 0, formatters.length);
        if (number == 0)
        {
            return;
        }

        display.setFormatter(formatters[number - 1]);
        System.out.println("Обрано формат: " + display.getFormatName());
    }

    static void showFormatExamples(Scanner scanner, Note[] notes, int noteCount)
    {
        if (noteCount == 0)
        {
            System.out.println("Поки немає створених нотаток.");
            return;
        }

        for (int i = 0; i < noteCount; i++)
        {
            System.out.printf("%d. %s%n", i + 1, notes[i].getTitle());
        }
        int number = readInt(scanner, "Оберіть нотатку (0 — назад): ", 0, noteCount);
        if (number == 0)
        {
            return;
        }

        Note note = notes[number - 1];
        NoteFormatter[] formatters = NoteFormatterFactory.createAll();

        for (NoteFormatter formatter : formatters)
        {
            System.out.println();
            System.out.println("Формат: " + formatter.getName());
            formatter.print(note);
        }
    }

    static void printStatistics(NoteStatisticsObserver statistics)
    {
        System.out.println("Додано за сеанс: " + statistics.getAddedCount());
        for (int priority = 1; priority <= 10; priority++)
        {
            System.out.printf("Пріоритет %d: %d%n",
                priority, statistics.getCountByPriority(priority));
        }
    }
}
