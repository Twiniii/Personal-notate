package database;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import exceptions.DataAccessException;

public final class DBConnection
{
    private final DataSource dataSource;

    private DBConnection()
    {
        Properties settings = new Properties();
        try (InputStream input = DBConnection.class.getResourceAsStream("/application.properties"))
        {
            if (input == null)
            {
                throw new IOException("Не знайдено application.properties.");
            }
            try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8))
            {
                settings.load(reader);
            }
            Path local = Path.of("config", "application-local.properties");
            if (Files.exists(local))
            {
                try (Reader reader = Files.newBufferedReader(local, StandardCharsets.UTF_8))
                {
                    settings.load(reader);
                }
            }
        }
        catch (IOException error)
        {
            throw new DataAccessException("Не вдалося прочитати налаштування БД.", error);
        }
        JdbcDataSource source = new JdbcDataSource();
        source.setURL(setting(settings, "db.url", "NOTATE_DB_URL"));
        source.setUser(setting(settings, "db.user", "NOTATE_DB_USER"));
        source.setPassword(setting(settings, "db.password", "NOTATE_DB_PASSWORD"));
        dataSource = source;
    }

    private static class Holder
    {
        private static final DBConnection INSTANCE = new DBConnection();
    }

    public static DBConnection getInstance()
    {
        return Holder.INSTANCE;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public Connection getConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

    private static String setting(Properties settings, String key, String environment)
    {
        String value = System.getenv(environment);
        return value != null ? value : settings.getProperty(key);
    }
}
