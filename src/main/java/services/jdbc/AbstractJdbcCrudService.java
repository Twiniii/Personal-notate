package services.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import exceptions.DataAccessException;
import exceptions.EntityNotFoundException;
import interfaces.CRUDService;
import models.BaseEntity;

public abstract class AbstractJdbcCrudService<T extends BaseEntity> implements CRUDService<T>
{
    protected final DataSource source;

    protected AbstractJdbcCrudService(DataSource source)
    {
        this.source = Objects.requireNonNull(source);
    }

    protected abstract String selectSql();
    protected abstract String saveSql();
    protected abstract String deleteSql();
    protected abstract T mapRow(ResultSet row) throws SQLException;
    protected abstract void setParameters(PreparedStatement statement, T entity) throws SQLException;

    protected void saveRelated(Connection connection, T entity) throws SQLException {}

    @Override
    public final T findById(long id)
    {
        List<T> values = query(selectSql() + " WHERE e.id = ?", id);
        if (values.isEmpty())
        {
            throw new EntityNotFoundException(id);
        }
        return values.getFirst();
    }

    @Override
    public final void save(T entity)
    {
        Objects.requireNonNull(entity, "Неможливо зберегти null замість сутності.");
        try (Connection connection = source.getConnection())
        {
            connection.setAutoCommit(false);
            try
            {
                try (PreparedStatement statement = connection.prepareStatement(saveSql()))
                {
                    setParameters(statement, entity);
                    statement.executeUpdate();
                }
                saveRelated(connection, entity);
                connection.commit();
            }
            catch (SQLException | RuntimeException error)
            {
                rollback(connection, error);
                throw error;
            }
        }
        catch (SQLException error)
        {
            throw failure(error);
        }
    }

    @Override
    public final boolean delete(long id)
    {
        try (Connection connection = source.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteSql()))
        {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
        catch (SQLException error)
        {
            throw failure(error);
        }
    }

    @Override
    public List<T> findAll()
    {
        return query(selectSql() + " ORDER BY e.created_order");
    }

    @Override
    public List<T> findAllSortedByName()
    {
        return query(selectSql() + " ORDER BY e.name, e.created_order");
    }

    @Override
    public List<T> findAllSorted(Comparator<? super T> comparator)
    {
        Objects.requireNonNull(comparator, "Компаратор не може бути null.");
        List<T> values = findAll();
        values.sort(comparator);
        return values;
    }

    public List<T> findPage(int limit, long offset)
    {
        if (limit < 1 || limit > 100 || offset < 0)
        {
            throw new IllegalArgumentException("Розмір сторінки: 1–100; зміщення: не менше 0.");
        }
        return query(selectSql() + " ORDER BY e.created_order LIMIT ? OFFSET ?", limit, offset);
    }

    protected final List<T> query(String sql, long... parameters)
    {
        try (Connection connection = source.getConnection())
        {
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(false);
            try
            {
                List<T> values = new ArrayList<>();
                try (PreparedStatement statement = connection.prepareStatement(sql))
                {
                    for (int i = 0; i < parameters.length; i++)
                    {
                        statement.setLong(i + 1, parameters[i]);
                    }
                    try (ResultSet result = statement.executeQuery())
                    {
                        while (result.next())
                        {
                            values.add(mapRow(result));
                        }
                    }
                }
                connection.commit();
                return values;
            }
            catch (SQLException | RuntimeException error)
            {
                rollback(connection, error);
                throw error;
            }
        }
        catch (SQLException error)
        {
            throw failure(error);
        }
    }

    protected static void rollback(Connection connection, Exception error)
    {
        try
        {
            connection.rollback();
        }
        catch (SQLException rollbackError)
        {
            error.addSuppressed(rollbackError);
        }
    }

    protected static DataAccessException failure(SQLException error)
    {
        String state = error.getSQLState();
        String message = state != null && state.startsWith("23")
            ? "Порушено обмеження БД: перевірте унікальність назви або зв’язані записи."
            : state != null && state.startsWith("22")
                ? "Некоректні дані для БД. Назва нотатки або категорії має містити не більше 255 символів."
                : "Помилка роботи з БД. Перевірте налаштування та доступність файлу.";
        return new DataAccessException(message, error);
    }
}
