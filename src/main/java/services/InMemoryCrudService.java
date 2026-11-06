package services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import exceptions.EntityNotFoundException;
import interfaces.CRUDService;
import models.BaseEntity;

public class InMemoryCrudService<T extends BaseEntity> implements CRUDService<T>
{
    private final Map<Long, T> storage = new LinkedHashMap<>();

    @Override
    public T findById(long id)
    {
        T entity = storage.get(id);
        if (entity == null)
        {
            throw new EntityNotFoundException(id);
        }
        return entity;
    }

    @Override
    public void save(T entity)
    {
        Objects.requireNonNull(entity, "Неможливо зберегти null замість сутності.");
        storage.put(entity.getId(), entity);
    }

    @Override
    public boolean delete(long id)
    {
        return storage.remove(id) != null;
    }

    @Override
    public List<T> findAll()
    {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<T> findAllSorted(Comparator<? super T> comparator)
    {
        Objects.requireNonNull(comparator, "Компаратор не може бути null.");
        List<T> result = findAll();
        result.sort(comparator);
        return result;
    }
}
