package interfaces;

import java.util.Comparator;
import java.util.List;
import comparators.EntityComparators;
import models.BaseEntity;

public interface CRUDService<T extends BaseEntity>
{
    T findById(long id);

    void save(T entity);

    boolean delete(long id);

    List<T> findAll();

    List<T> findAllSorted(Comparator<? super T> comparator);

    default List<T> findAllSortedByName()
    {
        return findAllSorted(EntityComparators.byName());
    }
}
