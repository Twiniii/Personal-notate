import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.h2.jdbcx.JdbcDataSource;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import database.*;
import exceptions.*;
import models.*;
import services.jdbc.*;
import services.Notebook;
import comparators.EntityComparators;

class JdbcServicesTest
{
    private JdbcDataSource source;
    private Connection keepAlive;
    private JdbcNoteService notes;
    private JdbcCategoryService categories;
    private JdbcIdSequence ids;

    @BeforeEach
    void prepare() throws Exception
    {
        source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:test_" + UUID.randomUUID() + ";COLLATION=UKRAINIAN");
        keepAlive = source.getConnection();
        SchemaInitializer.initialize(source);
        notes = new JdbcNoteService(source);
        categories = new JdbcCategoryService(source);
        ids = new JdbcIdSequence(source);
    }

    @AfterEach
    void close() throws Exception
    {
        keepAlive.close();
    }

    private TextNote text(String name, int priority) throws NoteException
    {
        return new TextNote(ids.next(), name, "Текст з апострофом ' та кирилицею", "Навчання", priority, "Книга");
    }

    @Test
    void crudAndStableOrder() throws Exception
    {
        Note first = text("Перша", 3);
        Note second = text("Друга", 2);
        notes.save(first);
        notes.save(second);
        notes.save(new TextNote(first.getId(), "Оновлена", "Новий текст", "Навчання", 1));
        assertEquals(2, notes.findAll().size());
        assertEquals("Оновлена", notes.findById(first.getId()).getName());
        assertEquals(List.of(first.getId(), second.getId()), notes.findAll().stream().map(Note::getId).toList());
        assertTrue(notes.delete(first.getId()));
        assertFalse(notes.delete(first.getId()));
        assertEquals(first.getId(), assertThrows(EntityNotFoundException.class,
            () -> notes.findById(first.getId())).getId());
        assertThrows(NullPointerException.class, () -> notes.save(null));
    }

    @Test
    void everySubtypeRoundTripsAndCanChangeType() throws Exception
    {
        TextNote a = text("Текст", 2);
        ChecklistNote b = new ChecklistNote(ids.next(), "Справи", "Опис", "Навчання", 3,
            new String[]{"Раз", "Два"}, new boolean[]{true, false});
        ShoppingListNote c = new ShoppingListNote(ids.next(), "Покупки", "Вечеря", "Особисте", 1,
            new String[]{"Хліб", "Молоко"}, new boolean[]{false, true}, "АТБ");
        for (Note note : List.of(a, b, c))
        {
            notes.save(note);
            Note restored = notes.findById(note.getId());
            assertEquals(note.getClass(), restored.getClass());
            assertTrue(note.hasSameContent(restored));
        }
        notes.save(new TextNote(c.getId(), "Вже текст", "", "Особисте", 4));
        assertInstanceOf(TextNote.class, notes.findById(c.getId()));
        assertEquals(0, count("SELECT COUNT(*) FROM note_items WHERE note_id = " + c.getId()));
        assertTrue(notes.delete(b.getId()));
        assertEquals(0, count("SELECT COUNT(*) FROM note_items"));
    }

    @Test
    void sqlSortingMatchesComparatorAndDoesNotMutateStorage() throws Exception
    {
        for (String name : List.of("Я", "Java", "І", "Java", "Є", "Ї", "А"))
        {
            notes.save(text(name, name.equals("Java") && notes.findAll().size() == 1 ? 9 : 2));
        }
        List<Long> before = notes.findAll().stream().map(Note::getId).toList();
        assertEquals(notes.findAllSorted(EntityComparators.byName()), notes.findAllSortedByName());
        assertEquals(notes.findAllSorted(Comparator.comparing(Note::getName, EntityComparators.ukrainianNames())
            .thenComparingInt(Note::getPriority)), notes.findAllSortedByNameAndByPriority());
        assertEquals(before, notes.findAll().stream().map(Note::getId).toList());
        notes.findAll().clear();
        assertEquals(before.size(), notes.findAll().size());
    }

    @Test
    void pagingHasBoundariesAndStableOrder() throws Exception
    {
        for (int i = 0; i < 5; i++) notes.save(text("Нотатка " + i, 1));
        assertEquals(notes.findAll().subList(2, 4), notes.findPage(2, 2));
        assertEquals(1, notes.findPage(2, 4).size());
        assertTrue(notes.findPage(2, 99).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> notes.findPage(0, 0));
        assertThrows(IllegalArgumentException.class, () -> notes.findPage(101, 0));
        assertThrows(IllegalArgumentException.class, () -> notes.findPage(1, -1));
    }

    @Test
    void categoryRenameAndDeleteRestriction() throws Exception
    {
        Note note = text("Моя", 1);
        notes.save(note);
        Category category = categories.findAll().getFirst();
        assertThrows(DataAccessException.class, () -> categories.delete(category.getId()));
        categories.save(new Category(category.getId(), "Нова назва", "Опис"));
        assertEquals("Нова назва", notes.findById(note.getId()).getCategory());
        assertThrows(DataAccessException.class, () -> categories.save(new Category(ids.next(), "Нова назва", "")));
        notes.delete(note.getId());
        assertTrue(categories.delete(category.getId()));
        assertFalse(categories.delete(category.getId()));
    }

    @Test
    void saveRollsBackAutoCreatedCategoryAfterSqlError() throws Exception
    {
        Note note = new TextNote(ids.next(), "x".repeat(256), "", "Новостворена", 1);
        assertThrows(DataAccessException.class, () -> notes.save(note));
        assertTrue(notes.findAll().isEmpty());
        assertTrue(categories.findAll().isEmpty());
    }

    @Test
    void transactionMovesNotesAndDeletesSourceCategory() throws Exception
    {
        Note note = text("Моя", 2);
        notes.save(note);
        long from = categories.findAll().getFirst().getId();
        long to = ids.next();
        categories.save(new Category(to, "Архів", ""));
        CategoryTransferService service = new CategoryTransferService(source);
        assertEquals(1, service.moveAndDelete(from, to));
        assertEquals("Архів", notes.findById(note.getId()).getCategory());
        assertThrows(EntityNotFoundException.class, () -> categories.findById(from));
        assertThrows(EntityNotFoundException.class, () -> service.moveAndDelete(to, Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> service.moveAndDelete(to, to));
    }

    @Test
    void transferRollsBackUpdateWhenLaterDeleteFails() throws Exception
    {
        Note note = text("Моя", 2);
        notes.save(note);
        long from = categories.findAll().getFirst().getId();
        long to = ids.next();
        categories.save(new Category(to, "Архів", ""));
        execute("CREATE TABLE test_guard(id BIGINT PRIMARY KEY, category_id BIGINT REFERENCES categories(id) ON DELETE RESTRICT)");
        try (PreparedStatement statement = keepAlive.prepareStatement("INSERT INTO test_guard VALUES (1, ?)"))
        {
            statement.setLong(1, from);
            statement.executeUpdate();
        }
        assertThrows(DataAccessException.class, () -> new CategoryTransferService(source).moveAndDelete(from, to));
        assertEquals("Навчання", notes.findById(note.getId()).getCategory());
        assertEquals(from, categories.findById(from).getId());
    }

    @Test
    void batchIsAtomicAndStoresAllRows()
    {
        categories.insertBatch(List.of(new Category(ids.next(), "А", ""), new Category(ids.next(), "Б", "")));
        assertEquals(2, categories.findAll().size());
        assertThrows(DataAccessException.class, () -> categories.insertBatch(
            List.of(new Category(ids.next(), "В", ""), new Category(ids.next(), "А", ""))));
        assertEquals(List.of("А", "Б"), categories.findAll().stream().map(Category::getName).toList());
    }

    @Test
    void injectionAndQuotesAreJustData() throws Exception
    {
        String attack = "' OR '1'='1";
        Note a = text(attack, 2);
        Note b = text("Пам’ять O'Brien", 3);
        notes.save(a);
        notes.save(b);
        assertEquals(attack, notes.findById(a.getId()).getName());
        try (PreparedStatement statement = keepAlive.prepareStatement("SELECT COUNT(*) FROM notes WHERE name = ?"))
        {
            statement.setString(1, attack);
            try (ResultSet result = statement.executeQuery())
            {
                result.next();
                assertEquals(1, result.getInt(1));
            }
        }
        assertEquals(2, notes.findAll().size());
    }

    @Test
    void schemaIsIdempotent() throws Exception
    {
        Note note = text("Не загубити", 1);
        notes.save(note);
        SchemaInitializer.initialize(source);
        assertEquals(1, notes.findAll().size());
        assertTrue(note.hasSameContent(notes.findById(note.getId())));
    }

    @Test
    void databaseRejectsInvalidPriorityAndMissingParent() throws Exception
    {
        Note note = text("Правильна", 1);
        notes.save(note);
        assertThrows(SQLException.class, () -> execute("UPDATE notes SET priority = 20"));
        assertThrows(SQLException.class, () -> execute("UPDATE notes SET category_id = 999999"));
        assertEquals(1, notes.findById(note.getId()).getPriority());
    }

    @Test
    void notebookWorksWithJdbcWithoutChangingItsCode() throws Exception
    {
        Notebook notebook = new Notebook(notes);
        Note note = text("Звична логіка", 2);
        int[] notifications = {0};
        notebook.addObserver((n, count) -> notifications[0]++);
        notebook.addNote(note);
        assertEquals(1, notebook.getNoteCount());
        assertEquals(1, notifications[0]);
        notebook.updateNote(new TextNote(note.getId(), "Оновлено", "", "Навчання", 1));
        assertEquals(1, notifications[0]);
        assertEquals("Оновлено", notebook.getNote(note.getId()).getName());
        assertTrue(notebook.deleteNote(note.getId()));
    }

    @Test
    void fileDatabaseSurvivesClosingEveryConnection(@TempDir Path directory) throws Exception
    {
        JdbcDataSource fileSource = new JdbcDataSource();
        fileSource.setURL("jdbc:h2:file:" + directory.resolve("saved").toAbsolutePath() + ";COLLATION=UKRAINIAN");
        SchemaInitializer.initialize(fileSource);
        JdbcIdSequence sequence = new JdbcIdSequence(fileSource);
        long id = sequence.next();
        Note original = new ShoppingListNote(id, "Збережені покупки", "", "Особисте", 2,
            new String[]{"Хліб", "Молоко"}, new boolean[]{true, false}, "АТБ");
        new JdbcNoteService(fileSource).save(original);
        JdbcDataSource reopened = new JdbcDataSource();
        reopened.setURL(fileSource.getURL());
        SchemaInitializer.initialize(reopened);
        assertTrue(original.hasSameContent(new JdbcNoteService(reopened).findById(id)));
        long next = new JdbcIdSequence(reopened).next();
        assertTrue(next > id);
        assertNotEquals(new JdbcCategoryService(reopened).findAll().getFirst().getId(), next);
    }

    @Test
    void singletonAndIndependentConnections() throws Exception
    {
        try (ExecutorService executor = Executors.newFixedThreadPool(4))
        {
            var calls = new ArrayList<Callable<DBConnection>>();
            for (int i = 0; i < 8; i++) calls.add(DBConnection::getInstance);
            for (var result : executor.invokeAll(calls))
                assertSame(DBConnection.getInstance(), result.get());
        }
        try (Connection a = source.getConnection(); Connection b = source.getConnection())
        {
            a.setAutoCommit(false);
            assertTrue(b.getAutoCommit());
            assertNotSame(a, b);
        }
    }

    @Test
    void previousCrudChecksStillPass() throws Exception
    {
        CrudServiceChecks.main(new String[0]);
    }

    private int count(String sql) throws SQLException
    {
        try (PreparedStatement statement = keepAlive.prepareStatement(sql); ResultSet result = statement.executeQuery())
        {
            result.next();
            return result.getInt(1);
        }
    }

    private void execute(String sql) throws SQLException
    {
        try (PreparedStatement statement = keepAlive.prepareStatement(sql))
        {
            statement.executeUpdate();
        }
    }
}
