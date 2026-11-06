package utils;

import java.util.Objects;
import java.util.function.LongSupplier;

public final class IdGenerator
{
    private long nextId = 1;
    private final LongSupplier supplier;

    public IdGenerator()
    {
        supplier = null;
    }

    public IdGenerator(LongSupplier supplier)
    {
        this.supplier = Objects.requireNonNull(supplier);
    }

    public long next()
    {
        if (supplier != null)
        {
            return supplier.getAsLong();
        }
        if (nextId == Long.MAX_VALUE)
        {
            throw new IllegalStateException("Доступні ідентифікатори вичерпано.");
        }
        return nextId++;
    }
}
