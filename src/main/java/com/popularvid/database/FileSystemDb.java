package com.popularvid.database;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that manages database i/o to and from a text file.
 *
 * Assumes first column always to be a primary key.
 *
 * @author Otar Magaldadze
 */
@Primary
@Repository
public class FileSystemDb implements DbInterface{

    private final String DB_DIR = "dbase";

    /**
     * Gets data from the database given the table name and key.
     *
     * @param table table name.
     * @param primaryKey key of the search row.
     * @return list of column entries for found row.
     * @throws IOException error with file i/o.
     */
    @Override
    public List<String> select(String table, String primaryKey) throws IOException {
        var result = new ArrayList<String>();

        List<String> rows = Files.readAllLines(Paths.get(DB_DIR, table));

        for (var row : rows) {
            var values = row.split(",");

            if (values[0].equals(primaryKey)) {
                result.addAll(Arrays.asList(values));
            }
        }

        return result;
    }

    /**
     * Insert new row into the given table.
     *
     * @param table table name.
     * @param values list of values for a new row.
     * @throws IOException error with file i/o.
     */
    @Override
    public void insert(String table, List<String> values) throws IOException {
        if (!select(table, values.get(0)).isEmpty()) {
            throw new KeyAlreadyExistsException("Username already exists!");
        }

        var data = new StringBuilder();
        data.append('\n');

        for (int i = 0; i < values.size(); i++) {
            data.append(values.get(i));

            if (i < values.size() - 1) {
                data.append(',');
            }
        }

        Files.writeString(Paths.get(DB_DIR, table),
                data.toString(),
                StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    /**
     * Edit existing data in a table.
     *
     * @param table table name.
     * @param values list of new values for the entire row.
     * @throws IOException error with file i/o.
     */
    @Override
    public void update(String table, List<String> values) throws IOException {
        List<String> rows = Files.readAllLines(Paths.get(DB_DIR, table));

        for (int i = 0; i < rows.size(); i++) {
            var oldValues = rows.get(i).split(",");

            if (oldValues[0].equals(values.get(0))) {
                rows.set(i, values.toString()
                        .replace(" ", "")
                        .replace("[", "")
                        .replace("]", ""));
            }
        }

        Files.write(Paths.get(DB_DIR, table),
                rows,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
