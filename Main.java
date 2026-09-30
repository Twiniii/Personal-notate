import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        
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

                int amount = readInt(
                    scanner,
                    "Скільки нотаток додати (1–" + freePlaces + "): ",
                    1,
                    freePlaces
                );

                
                for (int i = 0; i < amount; i++)
                {
                    System.out.println();
                    System.out.println("Нотатка №" + (noteCount + 1));

                    notes[noteCount] = readNote(scanner);
                    noteCount++;
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

                int searchPriority = readInt(
                    scanner,
                    "Введіть пріоритет для пошуку (1–10): ",
                    1,
                    10
                );

                int foundCount = 0;

                // Перевіряємо лише заповнену частину масиву.
                for (int i = 0; i < noteCount; i++)
                {
                    if (notes[i].priority == searchPriority)
                    {
                        System.out.println();
                        System.out.println("Нотатка №" + (i + 1));
                        System.out.println(notes[i]);

                        foundCount++;
                    }
                }

                if (foundCount == 0)
                {
                    System.out.println(
                        "Нотаток із таким пріоритетом не знайдено."
                    );
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

                System.out.println();
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

                System.out.println(
                    "Введіть усі поля нотатки, яку потрібно знайти."
                );

                Note sample = readNote(scanner);

                int index = findNote(notes, noteCount, sample);

                if (index == -1)
                {
                    System.out.println(
                        "Нотатку з повним збігом не знайдено."
                    );
                }
                else
                {
                    System.out.println(
                        "Знайдено нотатку №" + (index + 1)
                    );

                    System.out.println(notes[index]);
                }
            }
            else if (choice.equals("0"))
            {
                break;
            }
            else
            {
                System.out.println(
                    "Такого варіанту немає, спробуйте ще раз."
                );
            }
        }

        System.out.println("До побачення!");
        scanner.close();
    }

    
    static Note readNote(Scanner scanner)
    {
        System.out.print("Введіть назву нотатки: ");
        String title = scanner.nextLine();

        System.out.print("Введіть текст нотатки: ");
        String text = scanner.nextLine();

        System.out.print("Введіть категорію нотатки: ");
        String category = scanner.nextLine();

        int priority = readInt(
            scanner,
            "Введіть пріоритет (1 — найвищий, 10 — найнижчий): ",
            1,
            10
        );

        return new Note(title, text, category, priority);
    }

  
    static int readInt(
        Scanner scanner,
        String message,
        int min,
        int max
    )
    {
        while (true)
        {
            System.out.print(message);

            // Перевіряємо введення перед викликом nextInt().
            if (!scanner.hasNextInt())
            {
                System.out.println("Потрібно ввести ціле число.");

                scanner.nextLine();
                continue;
            }

            int value = scanner.nextInt();
            scanner.nextLine();

            if (value >= min && value <= max)
            {
                return value;
            }

            System.out.printf(
                "Введіть число від %d до %d.%n", min, max
            );
        }
    }

   
    static void printNotes(Note[] notes)
    {
        int number = 1;

        for (Note note : notes)
        {
            // Пропускаємо незаповнені місця.
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