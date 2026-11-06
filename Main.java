import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

public class Main
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        try
        {
            Note[] notes = new Note[100];
            int noteCount = 0;
            System.out.println("   Personal - Notate   ");

            while (true)
            {
                System.out.println();
                System.out.println("1. Додати нотатки");
                System.out.println("2. Переглянути нотатки");
                System.out.println("3. Показати та порахувати за пріоритетом");
                System.out.println("4. Відсортувати за пріоритетом");
                System.out.println("5. Знайти нотатку за повним збігом");
                System.out.println("6. Відкрити нотатку за номером");
                System.out.println("0. Вийти");
                System.out.print("Оберіть дію: ");
                String choice = scanner.nextLine();

                if (choice.equals("1"))
                {
                    if (noteCount >= notes.length)
                    {
                        System.out.println("Масив нотаток заповнений.");
                        continue;
                    }
                    int freePlaces = notes.length - noteCount;
                    int amount = readInt(scanner,
                        "Скільки нотаток додати (1–" + freePlaces + "): ",
                        1, freePlaces);

                    for (int i = 0; i < amount; i++)
                    {
                        boolean created = false;

                        while (!created)
                        {
                            System.out.println("Нотатка №" + (noteCount + 1));
                            try
                            {
                                notes[noteCount] = readNote(scanner);

                                noteCount++;
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
                    printNotes(notes);
                }
                else if (choice.equals("3"))
                {
                    if (noteCount == 0)
                    {
                        System.out.println("Поки немає створених нотаток.");
                        continue;
                    }
                    int priority = readInt(scanner,
                        "Введіть пріоритет для пошуку (1–10): ", 1, 10);
                    int foundCount = 0;
                    for (int i = 0; i < noteCount; i++)
                    {
                        if (notes[i].priority == priority)
                        {
                            System.out.println("Нотатка №" + (i + 1));
                            System.out.println(notes[i]);
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
                    if (noteCount == 0)
                    {
                        System.out.println("Поки немає створених нотаток.");
                        continue;
                    }
                    System.out.println("До сортування:");
                    printNotes(notes);
                    sortByPriority(notes, noteCount);
                    System.out.println("Після сортування:");
                    printNotes(notes);
                }
                else if (choice.equals("5"))
                {
                    if (noteCount == 0)
                    {
                        System.out.println("Поки немає створених нотаток.");
                        continue;
                    }
                    try
                    {
                        System.out.println("Введіть усі поля нотатки-зразка.");
                        Note sample = readNote(scanner);
                        int index = findNote(notes, noteCount, sample);
                        if (index == -1)
                        {
                            System.out.println("Нотатку з повним збігом не знайдено.");
                        }
                        else
                        {
                            System.out.println("Знайдено нотатку №" + (index + 1));
                            System.out.println(notes[index]);
                        }
                    }
                    catch (NoteException error)
                    {


                        System.out.println("Пошук скасовано: " + error.getMessage());
                    }
                }
                else if (choice.equals("6"))
                {
                    openNote(scanner, notes, noteCount);
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

    static void openNote(Scanner scanner, Note[] notes, int noteCount)
    {
        if (noteCount == 0)
        {
            System.out.println("Поки немає створених нотаток.");
            return;
        }

        for (int i = 0; i < noteCount; i++)
        {
            System.out.printf("%d. %s%n", i + 1, notes[i].title);
        }
        System.out.print("Введіть номер нотатки (0 — назад): ");
        try
        {
            int number = Integer.parseInt(scanner.nextLine().trim());
            if (number == 0)
            {
                return;
            }
            System.out.println(getNote(notes, noteCount, number));
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

    static Note getNote(Note[] notes, int noteCount, int number)
    {
        if (number < 1 || number > noteCount)
        {
            throw new IndexOutOfBoundsException(
                "Нотатки з номером " + number
                + " немає. Допустимі номери: 1–" + noteCount + "."
            );
        }
        return notes[number - 1];
    }

    static void printNotes(Note[] notes)
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
            System.out.println(note);
            number++;
        }
        if (number == 1)
        {
            System.out.println("Поки немає створених нотаток.");
        }
    }

    static void sortByPriority(Note[] notes, int noteCount)
    {
        for (int i = 0; i < noteCount - 1; i++)
        {
            for (int j = 0; j < noteCount - 1 - i; j++)
            {
                if (notes[j].priority > notes[j + 1].priority)
                {
                    Note temporary = notes[j];
                    notes[j] = notes[j + 1];
                    notes[j + 1] = temporary;
                }
            }
        }
    }

    static int findNote(Note[] notes, int noteCount, Note sample)
    {
        for (int i = 0; i < noteCount; i++)
        {
            if (notes[i].equals(sample))
            {
                return i;
            }
        }
        return -1;
    }
}
