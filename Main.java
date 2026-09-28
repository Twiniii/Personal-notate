import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        Note[] notes = new Note[100];
        int noteCount = 0;

        System.out.println(" Personal - Notate ");
        System.out.println();

        while (true)
        {
            System.out.println();
            System.out.println("1. Створити нотатку");
            System.out.println("2. Переглянути нотатки");
            System.out.println("3. Показати нотатки за пріоритетом");
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

                System.out.print("Введіть назву нотатки: ");
                String title = scanner.nextLine();

                System.out.print("Введіть текст нотатки: ");
                String text = scanner.nextLine();

                System.out.print("Введіть категорію нотатки: ");
                String category = scanner.nextLine();

                System.out.print("Введіть пріоритет нотатки (1-10): ");
                int priority = scanner.nextInt();
                scanner.nextLine();

                notes[noteCount] = new Note(
                    title,
                    text,
                    category,
                    priority
                );

                noteCount++;

                System.out.println("Нотатку збережено.");
            }

            else if (choice.equals("2"))
            {
                if (noteCount == 0)
                {
                    System.out.println("Поки немає створених нотаток.");
                }
                else
                {
                    for (int i = 0; i < noteCount; i++)
                    {
                        System.out.println();
                        System.out.println("Нотатка №" + (i + 1));
                        System.out.println(notes[i]);
                    }
                }
            }

            else if (choice.equals("3"))
            {
                if (noteCount == 0)
                {
                    System.out.println("Поки немає створених нотаток.");
                }
                else
                {
                    System.out.print("Введіть пріоритет для пошуку (1-10): ");
                    int searchPriority = scanner.nextInt();
                    scanner.nextLine();

                    boolean found = false;

                    for (int i = 0; i < noteCount; i++)
                    {
                        if (notes[i].priority == searchPriority)
                        {
                            System.out.println();
                            System.out.println("Нотатка №" + (i + 1));
                            System.out.println(notes[i]);

                            found = true;
                        }
                    }

                    if (!found)
                    {
                        System.out.println(
                            "Нотаток з таким пріоритетом не знайдено."
                        );
                    }
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
}