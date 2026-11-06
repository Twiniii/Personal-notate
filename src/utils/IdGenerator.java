package utils;

public final class IdGenerator
{
    private long nextId = 1;

    public long next()
    {
        if (nextId == Long.MAX_VALUE)
        {
            throw new IllegalStateException("Доступні ідентифікатори вичерпано.");
        }
        return nextId++;
    }
}
