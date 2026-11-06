package demo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import database.SchemaInitializer;
import database.JdbcIdSequence;
import exceptions.DataAccessException;
import exceptions.EntityNotFoundException;
import exceptions.NoteException;
import models.Category;
import models.TextNote;
import services.jdbc.JdbcNoteService;
import services.jdbc.JdbcCategoryService;
import services.jdbc.CategoryTransferService;

public final class JdbcDemo
{
    private JdbcDemo() {}

    public static void main(String[] args)
    {
        run();
    }

    public static void run()
    {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:demo_" + UUID.randomUUID() + ";COLLATION=UKRAINIAN");
        try (Connection keepAlive = source.getConnection())
        {
            SchemaInitializer.initialize(source);
            SchemaInitializer.initialize(source);
            JdbcIdSequence ids = new JdbcIdSequence(source);
            JdbcNoteService notes = new JdbcNoteService(source);
            JdbcCategoryService categories = new JdbcCategoryService(source);
            long first = ids.next();
            notes.save(new TextNote(first, "Java", "До оновлення", "Навчання", 7));
            notes.save(new TextNote(ids.next(), "Java", "Друга", "Навчання", 2));
            notes.save(new TextNote(ids.next(), "Алгоритми", "Третя", "Навчання", 4));
            notes.save(new TextNote(first, "Java", "Після оновлення", "Навчання", 7));
            System.out.println("Після оновлення кількість: " + notes.findAll().size());
            System.out.println("Порядок сховища:");
            notes.findAll().forEach(n -> System.out.println(n.getName() + " / " + n.getPriority()));
            System.out.println("SQL ORDER BY name, priority:");
            notes.findAllSortedByNameAndByPriority().forEach(n ->
                System.out.println(n.getName() + " / " + n.getPriority()));
            System.out.println("Сховище після сортування:");
            notes.findAll().forEach(n -> System.out.println(n.getName() + " / " + n.getPriority()));
            System.out.println("Сторінка з двох: " + notes.findPage(2, 0).size());
            try
            {
                notes.findById(Long.MAX_VALUE);
            }
            catch (EntityNotFoundException error)
            {
                System.out.println("Очікувано: " + error.getMessage());
            }
            int beforeNotes = notes.findAll().size();
            int beforeCategories = categories.findAll().size();
            try
            {
                notes.save(new TextNote(ids.next(), "x".repeat(256), "", "Категорія для відкату", 2));
            }
            catch (DataAccessException expected)
            {
                System.out.println("ROLLBACK: категорію вже вставляли, але назва нотатки перевищила 255 символів.");
            }
            System.out.println("Нотаток після відкату: " + notes.findAll().size() + ", було " + beforeNotes);
            System.out.println("Категорій після відкату: " + categories.findAll().size() + ", було " + beforeCategories);
            long from = categories.findAll().getFirst().getId();
            long to = ids.next();
            categories.save(new Category(to, "Архів", "Для завершених записів"));
            System.out.println("COMMIT: перенесено " + new CategoryTransferService(source).moveAndDelete(from, to));
            try (PreparedStatement statement = keepAlive.prepareStatement("SELECT COUNT(*) FROM notes WHERE name = ?"))
            {
                statement.setString(1, "' OR '1'='1");
                try (ResultSet result = statement.executeQuery())
                {
                    result.next();
                    System.out.println("Спроба SQL-ін’єкції як параметр: збігів " + result.getInt(1));
                }
            }
            System.out.println("delete наявного: " + notes.delete(first));
            System.out.println("delete повторно: " + notes.delete(first));
            System.out.println("Демонстрація завершена. Ваш блокнот не змінювався.");
        }
        catch (SQLException | NoteException error)
        {
            throw new DataAccessException("Не вдалося виконати демонстрацію JDBC.", error);
        }
    }
}
