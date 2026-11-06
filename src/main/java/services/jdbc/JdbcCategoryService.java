package services.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import database.JdbcIdSequence;
import models.Category;

public final class JdbcCategoryService extends AbstractJdbcCrudService<Category>
{
    public JdbcCategoryService(DataSource source)
    {
        super(source);
    }

    @Override
    protected String selectSql()
    {
        return "SELECT e.id, e.name, e.description FROM categories e";
    }

    @Override
    protected String saveSql()
    {
        return "MERGE INTO categories (id, name, description) KEY(id) VALUES (?, ?, ?)";
    }

    @Override
    protected String deleteSql()
    {
        return "DELETE FROM categories WHERE id = ?";
    }

    @Override
    protected Category mapRow(ResultSet row) throws SQLException
    {
        return new Category(row.getLong("id"), row.getString("name"), row.getString("description"));
    }

    @Override
    protected void setParameters(PreparedStatement statement, Category category) throws SQLException
    {
        statement.setLong(1, category.getId());
        statement.setString(2, category.getName());
        statement.setString(3, category.getDescription());
    }

    static long resolve(Connection connection, String name) throws SQLException
    {
        String label = name == null || name.isBlank() ? "Без категорії" : name;
        try (PreparedStatement find = connection.prepareStatement("SELECT id FROM categories WHERE name = ?"))
        {
            find.setString(1, label);
            try (ResultSet result = find.executeQuery())
            {
                if (result.next())
                {
                    return result.getLong(1);
                }
            }
        }
        long id = JdbcIdSequence.next(connection);
        try (PreparedStatement insert = connection.prepareStatement(
            "INSERT INTO categories(id, name, description) VALUES (?, ?, ?)"))
        {
            insert.setLong(1, id);
            insert.setString(2, label);
            insert.setString(3, "");
            insert.executeUpdate();
        }
        return id;
    }

    public void insertBatch(List<Category> categories)
    {
        insertMany(categories, true);
    }

    public void insertIndividually(List<Category> categories)
    {
        insertMany(categories, false);
    }

    private void insertMany(List<Category> categories, boolean batch)
    {
        Objects.requireNonNull(categories);
        try (Connection connection = source.getConnection())
        {
            connection.setAutoCommit(false);
            try
            {
                try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO categories(id, name, description) VALUES (?, ?, ?)"))
                {
                    for (Category category : categories)
                    {
                        setParameters(statement, Objects.requireNonNull(category));
                        if (batch)
                        {
                            statement.addBatch();
                        }
                        else
                        {
                            statement.executeUpdate();
                        }
                    }
                    if (batch)
                    {
                        statement.executeBatch();
                    }
                }
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
}
