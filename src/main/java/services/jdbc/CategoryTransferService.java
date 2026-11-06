package services.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import exceptions.EntityNotFoundException;

public final class CategoryTransferService
{
    private final DataSource source;

    public CategoryTransferService(DataSource source)
    {
        this.source = source;
    }

    public int moveAndDelete(long fromId, long toId)
    {
        if (fromId == toId)
        {
            throw new IllegalArgumentException("Оберіть дві різні категорії.");
        }
        try (Connection connection = source.getConnection())
        {
            connection.setAutoCommit(false);
            try
            {
                requireCategory(connection, Math.min(fromId, toId));
                requireCategory(connection, Math.max(fromId, toId));
                int moved;
                try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE notes SET category_id = ? WHERE category_id = ?"))
                {
                    statement.setLong(1, toId);
                    statement.setLong(2, fromId);
                    moved = statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM categories WHERE id = ?"))
                {
                    statement.setLong(1, fromId);
                    statement.executeUpdate();
                }
                connection.commit();
                return moved;
            }
            catch (SQLException | RuntimeException error)
            {
                AbstractJdbcCrudService.rollback(connection, error);
                throw error;
            }
        }
        catch (SQLException error)
        {
            throw AbstractJdbcCrudService.failure(error);
        }
    }

    private static void requireCategory(Connection connection, long id) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM categories WHERE id = ? FOR UPDATE"))
        {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery())
            {
                if (!result.next())
                {
                    throw new EntityNotFoundException(id);
                }
            }
        }
    }
}
