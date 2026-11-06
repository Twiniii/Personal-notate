package database;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import exceptions.DataAccessException;

public final class SchemaInitializer
{
    private SchemaInitializer() {}

    public static void initialize(DataSource source)
    {
        try (InputStream input = SchemaInitializer.class.getResourceAsStream("/init.sql"))
        {
            if (input == null)
            {
                throw new IOException("Не знайдено init.sql.");
            }
            String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection connection = source.getConnection())
            {
                for (String sql : script.split(";"))
                {
                    if (!sql.isBlank())
                    {
                        try (PreparedStatement statement = connection.prepareStatement(sql))
                        {
                            statement.execute();
                        }
                    }
                }
            }
        }
        catch (IOException | SQLException error)
        {
            throw new DataAccessException("Не вдалося підготувати схему БД.", error);
        }
    }
}
