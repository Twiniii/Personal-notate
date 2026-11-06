package demo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.h2.jdbcx.JdbcDataSource;
import database.SchemaInitializer;
import models.Category;
import services.jdbc.JdbcCategoryService;

public final class BatchBenchmark
{
    private BatchBenchmark() {}

    public static void main(String[] args) throws Exception
    {
        int count = 3000;
        List<Category> values = new ArrayList<>();
        for (int i = 1; i <= count; i++)
        {
            values.add(new Category(i, "Категорія " + i, "Пакетне додавання"));
        }
        run(values, false);
        run(values, true);
        double[] single = new double[5];
        double[] batch = new double[5];
        for (int i = 0; i < single.length; i++)
        {
            if (i % 2 == 0)
            {
                single[i] = run(values, false);
                batch[i] = run(values, true);
            }
            else
            {
                batch[i] = run(values, true);
                single[i] = run(values, false);
            }
        }
        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Rows: " + count + "; file H2; one transaction per run; milliseconds");
        System.out.println("Individual: " + Arrays.toString(single));
        System.out.println("Batch: " + Arrays.toString(batch));
        Arrays.sort(single);
        Arrays.sort(batch);
        System.out.printf(Locale.ROOT, "Median individual: %.3f ms%nMedian batch: %.3f ms%n", single[2], batch[2]);
    }

    private static double run(List<Category> values, boolean batch) throws Exception
    {
        Path directory = Files.createTempDirectory("notate-benchmark-");
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:file:" + directory.resolve("bench").toAbsolutePath() + ";COLLATION=UKRAINIAN");
        try (Connection keepAlive = source.getConnection())
        {
            SchemaInitializer.initialize(source);
            JdbcCategoryService service = new JdbcCategoryService(source);
            long start = System.nanoTime();
            if (batch)
            {
                service.insertBatch(values);
            }
            else
            {
                service.insertIndividually(values);
            }
            double milliseconds = (System.nanoTime() - start) / 1_000_000.0;
            if (service.findAll().size() != values.size())
            {
                throw new IllegalStateException("Не всі рядки збережено.");
            }
            return milliseconds;
        }
        finally
        {
            try (var files = Files.list(directory))
            {
                for (Path file : files.toList())
                {
                    Files.delete(file);
                }
            }
            Files.delete(directory);
        }
    }
}
