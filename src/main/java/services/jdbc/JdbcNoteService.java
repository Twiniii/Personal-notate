package services.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import exceptions.NoteException;
import interfaces.NoteService;
import models.Note;
import models.TextNote;
import models.ChecklistNote;
import models.ShoppingListNote;

public final class JdbcNoteService extends AbstractJdbcCrudService<Note> implements NoteService
{
    public JdbcNoteService(DataSource source)
    {
        super(source);
    }

    @Override
    protected String selectSql()
    {
        return "SELECT e.*, c.name AS category_name FROM notes e JOIN categories c ON c.id = e.category_id";
    }

    @Override
    protected String saveSql()
    {
        return "MERGE INTO notes (id, name, note_text, category_id, priority, note_type, source, store) "
            + "KEY(id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String deleteSql()
    {
        return "DELETE FROM notes WHERE id = ?";
    }

    @Override
    protected void setParameters(PreparedStatement statement, Note note) throws SQLException
    {
        long categoryId = JdbcCategoryService.resolve(statement.getConnection(), note.getCategory());
        String type = switch (note)
        {
            case ShoppingListNote shopping -> "SHOPPING";
            case ChecklistNote checklist -> "CHECKLIST";
            case TextNote text -> "TEXT";
        };
        statement.setLong(1, note.getId());
        statement.setString(2, note.getName());
        statement.setString(3, note.getText() == null ? "" : note.getText());
        statement.setLong(4, categoryId);
        statement.setInt(5, note.getPriority());
        statement.setString(6, type);
        statement.setString(7, note instanceof TextNote text ? text.getSource() : "");
        statement.setString(8, note instanceof ShoppingListNote shopping ? shopping.getStore() : "");
    }

    @Override
    protected void saveRelated(Connection connection, Note note) throws SQLException
    {
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM note_items WHERE note_id = ?"))
        {
            delete.setLong(1, note.getId());
            delete.executeUpdate();
        }
        if (note instanceof ChecklistNote checklist)
        {
            try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO note_items(note_id, position, item_text, completed) VALUES (?, ?, ?, ?)"))
            {
                for (int i = 0; i < checklist.getItemCount(); i++)
                {
                    insert.setLong(1, note.getId());
                    insert.setInt(2, i);
                    insert.setString(3, checklist.getItem(i));
                    insert.setBoolean(4, checklist.isCompleted(i));
                    insert.addBatch();
                }
                insert.executeBatch();
            }
        }
    }

    @Override
    protected Note mapRow(ResultSet row) throws SQLException
    {
        long id = row.getLong("id");
        String name = row.getString("name");
        String text = row.getString("note_text");
        String category = row.getString("category_name");
        int priority = row.getInt("priority");
        String type = row.getString("note_type");
        try
        {
            if (type.equals("TEXT"))
            {
                return new TextNote(id, name, text, category, priority, row.getString("source"));
            }
            List<String> items = new ArrayList<>();
            List<Boolean> states = new ArrayList<>();
            try (PreparedStatement statement = row.getStatement().getConnection().prepareStatement(
                "SELECT item_text, completed FROM note_items WHERE note_id = ? ORDER BY position"))
            {
                statement.setLong(1, id);
                try (ResultSet result = statement.executeQuery())
                {
                    while (result.next())
                    {
                        items.add(result.getString(1));
                        states.add(result.getBoolean(2));
                    }
                }
            }
            boolean[] completed = new boolean[states.size()];
            for (int i = 0; i < completed.length; i++)
            {
                completed[i] = states.get(i);
            }
            return switch (type)
            {
                case "CHECKLIST" -> new ChecklistNote(id, name, text, category, priority,
                    items.toArray(String[]::new), completed);
                case "SHOPPING" -> new ShoppingListNote(id, name, text, category, priority,
                    items.toArray(String[]::new), completed, row.getString("store"));
                default -> throw new SQLException("Невідомий тип нотатки: " + type);
            };
        }
        catch (NoteException | IllegalArgumentException error)
        {
            throw new SQLException("Збережена нотатка порушує правила моделі, ID: " + id, error);
        }
    }

    @Override
    public List<Note> findAllSortedByNameAndByPriority()
    {
        return query(selectSql() + " ORDER BY e.name, e.priority, e.created_order");
    }
}
