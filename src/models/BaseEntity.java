package models;

import diagnostics.ConstructorTrace;

public abstract class BaseEntity
{
    private final long id;
    private final String name;

    protected BaseEntity(long id, String name)
    {
        if (id <= 0)
        {
            throw new IllegalArgumentException("ID повинен бути додатним.");
        }
        if (name == null || name.isBlank())
        {
            throw new IllegalArgumentException("Назва сутності не може бути порожньою.");
        }
        this.id = id;
        this.name = name;
        ConstructorTrace.log("BaseEntity: ID та name");
    }

    public final long getId()
    {
        return id;
    }

    public final String getName()
    {
        return name;
    }

    @Override
    public final boolean equals(Object object)
    {
        return this == object
            || object instanceof BaseEntity other && id == other.id;
    }

    @Override
    public final int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return "ID: " + id + "\nНазва: " + name;
    }
}
