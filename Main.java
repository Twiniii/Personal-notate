import java.util.Scanner;

void main()
{
Scanner scanner = new Scanner(System.in);

IO.println("   Personal - Notate   ");
IO.print("Введіть назву нотатки: ");
String title = scanner.nextLine();

IO.print("Введіть текст нотатки:");
String content = scanner.nextLine();

IO.println();
IO.println("   Нотатка   ");
System.out.printf("Назва: %s%n", title);
System.out.printf("Опис:  %s%n", content);
IO.println("                  ");

scanner.close();











}