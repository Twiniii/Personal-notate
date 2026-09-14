import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

void main()
{
Scanner scanner = new Scanner(System.in);
Path notesFile = Path.of("notes.txt");

IO.println("   Personal - Notate   ");
IO.println("1 Створити та зберегти нотатку");
IO.println("2 Переглянути усі збережені нотатки");
IO.println("Оберіть дію: ");
String choice = scanner.nextLine();

try
{
if (choice.equals("1"))
{
IO.print("Введіть назву нотатки: ");
String title = scanner.nextLine();

IO.print("Введіть текст нотатки: ");
String content = scanner.nextLine();

String note = String.format("Назва: %s%nОпис: %s%n%n", title, content);

Files.writeString(notesFile, note, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

IO.println();
IO.println("   Нотатка   ");
IO.print(note);
System.out.println("Нотатку збережено");
}
else if (choice.equals("2"))
{
if (Files.exists(notesFile))
{
IO.println("    Збережені нотатки    ");
IO.print(Files.readString(notesFile));
}
else
{
IO.println("   Збережених нотаток не знайдено    ");
}
}
}
catch (IOException error)
{
IO.println("Помилка роботи з файлом: " + error.getMessage());
}

scanner.close();
}