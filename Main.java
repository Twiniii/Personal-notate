import java.util.Scanner;

public class Main
{
public static void main(String[] args)
{
Scanner scanner = new Scanner(System.in);

String title = "";
String text = "";
String category = "";
boolean hasNote = false;
int priority = 0;
double ompleteMinutes = 0.0;



System.out.println(" Personal - Notate ");
System.out.println();

while (true) 
{
System.out.println("1. Створити нотатку");
System.out.println("2. Переглянути останню нотатку");
System.out.println("0. Вийти");
String choice = scanner.nextLine();



if (choice.equals("1"))
{
System.out.println("Введіть Назву нотатки: ");
title = scanner.nextLine();

System.out.println("Введіть текст нотатки");
text = scanner.nextLine();

System.out.println("Введіть категорію нотатки");
category = scanner.nextLine();

System.out.println("Введіть приорітет нотатки (10-1)");
priority = scanner.nextInt();
scanner.nextLine();

hasNote = true;

System.out.printf("Назва: %s%nТекст: %s%nКатегорія: %s%n Приорітет: %d%n", title, text, category, priority);

}
else if (choice.equals("2"))
{
if(hasNote)
{
System.out.printf("Назва: %s%nТекст: %s%nКатегорія: %s%n Приорітет: %d%n", title, text, category, priority);
}

else
{
System.out.println(" Поки немає створених нотаток ");
}

}

else if (choice.equals("0"))
{
break;
}

else
{
System.out.println("Такого варіанту немає, спробуйте ще раз");
}

}
System.out.println("Cycle finished");


scanner.close();
}
}