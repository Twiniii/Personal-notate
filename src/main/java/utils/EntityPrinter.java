package utils;

import java.util.Collection;
import java.util.Objects;
import models.BaseEntity;

public final class EntityPrinter
{
    private EntityPrinter()
    {
    }

    public static void printAll(Collection<? extends BaseEntity> items)
    {
        Objects.requireNonNull(items, "Колекція не може бути null.");
        if (items.isEmpty())
        {
            System.out.println("Список порожній.");
        }
        for (BaseEntity item : items)
        {
            System.out.println(item);
            System.out.println();
        }
    }
}
