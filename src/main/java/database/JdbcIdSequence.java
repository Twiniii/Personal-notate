package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import exceptions.DataAccessException;

public final class JdbcIdSequence
{
    private final DataSource source;

    public JdbcIdSequence(DataSource source)
    {
        this.source = source;
    }

    public long next()
    {
        try (Connection connection = source.getConnection())
        {
            return next(connection);
        }
        catch (SQLException error)
        {
            throw new DataAccessException("Не вдалося отримати новий ID.", error);
        }
    }

    public static long next(Connection connection) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement("SELECT NEXT VALUE FOR entity_id_seq");
             ResultSet result = statement.executeQuery())
        {
            result.next();
            return result.getLong(1);
        }
    }
}
