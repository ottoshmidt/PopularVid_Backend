package com.popularvid.database;

import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

/**
 * Database abstraction interface.
 *
 * Provides main functionality to create and access data in
 * and from the physical database.
 *
 * @author Otar Magaldadze
 */
@Repository
public interface DbInterface {

    /**
     * Getting data from the database given table and key.
     *
     * @param table table name.
     * @param primaryKey key for search.
     * @return list of row values.
     * @throws IOException database i/o exception.
     */
    List<String> select(String table, String primaryKey) throws IOException;

    /**
     * Inserting data into given table into the database.
     *
     * @param table table name.
     * @param values entry values.
     * @throws IOException database i/o exception.
     */
    void insert(String table, List<String> values) throws IOException;

    /**
     * Updating data in the table of the database.
     *
     * @param table table name.
     * @param values values of a row to be updated.
     * @throws IOException database i/o exception.
     */
    void update(String table, List<String> values) throws IOException;
}
