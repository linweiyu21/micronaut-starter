package io.micronaut.starter.cli.feature.linweiyu.entity.metadata;

import io.micronaut.starter.cli.feature.linweiyu.entity.config.EntityGenerateSettings;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fetches metadata for all tables in a given database.
 */
@Slf4j
public class TableMetadataFetcher {

    private static final String[] TABLE_TYPES = new String[]{"TABLE", "VIEW"};

    private DatabaseMetaData getMetadata(EntityGenerateSettings settings) throws SQLException {
        try {
            Class.forName(settings.getDriverClassName());
        } catch (ClassNotFoundException e) {
            log.error("Failed to load JDBC driver (driver: {}, error: {})", settings.getDriverClassName(), e.getMessage(), e);
            throw new SQLException(e);
        }
        Connection connection = DriverManager.getConnection(settings.getUrl(), settings.getUsername(), settings.getPassword());
        return connection.getMetaData();
    }

    public List<String> getTableNames(EntityGenerateSettings entityGenerateSettings) throws SQLException {
        DatabaseMetaData databaseMeta = getMetadata(entityGenerateSettings);
        try {
            List<String> tableNames = new ArrayList<>();
            try (ResultSet rs = databaseMeta.getTables(null, entityGenerateSettings.getSchemaPattern(), "%", TABLE_TYPES)) {
                while (rs.next()) {
                    tableNames.add(rs.getString("TABLE_NAME"));
                }
            }
            return tableNames;
        } finally {
            databaseMeta.getConnection().close();
        }
    }

    public Table getTable(EntityGenerateSettings entityGenerateSettings, String schemaAndTable) throws SQLException {

        Table tableInfo = new Table();

        String schema = extractSchema(schemaAndTable);
        String table = extractTabeName(schemaAndTable);
        tableInfo.setName(table);
        tableInfo.setSchema(Optional.ofNullable(schema));
        DatabaseMetaData databaseMeta = getMetadata(entityGenerateSettings);
        try {
            try (ResultSet rs = databaseMeta.getTables(null, schema, table, TABLE_TYPES)) {
                if (rs.next()) {
                    tableInfo.setDescription(Optional.ofNullable(rs.getString("REMARKS")));
                }
            } catch (Exception e) {
                log.debug("Failed to fetch table comment", e);
            }

            final List<String> primaryKeyNames = new ArrayList<>();
            try (ResultSet rs = databaseMeta.getPrimaryKeys(null, schema, table)) {
                while (rs.next()) {
                    primaryKeyNames.add(rs.getString("COLUMN_NAME"));
                }
            }
            try (ResultSet rs = databaseMeta.getColumns(null, schema, table, "%")) {
                while (rs.next()) {
                    Column column = new Column();
                    column.setName(rs.getString("COLUMN_NAME"));
                    column.setTypeCode(rs.getInt("DATA_TYPE"));
                    column.setTypeName(rs.getString("TYPE_NAME"));


                    // Oracle throws java.sql.SQLException: Invalid column name
                    boolean autoIncrement = false;
                    try {
                        String autoIncrementMetadata = rs.getString("IS_AUTOINCREMENT");
                        autoIncrement = autoIncrementMetadata != null && autoIncrementMetadata.equals("YES");
                    } catch (Exception e) {
                        log.debug("Failed to fetch auto_increment flag for {}.{}", table, column.getName(), e);
                    }
                    column.setAutoIncrement(autoIncrement);

                    try {
                        column.setDescription(Optional.ofNullable(rs.getString("REMARKS")));
                    } catch (Exception e) {
                        log.debug("Failed to fetch comment flag for {}.{}", table, column.getName(), e);
                    }

                    boolean nullable = true;
                    try {
                        String isNullableMetadata = rs.getString("IS_NULLABLE");
                        nullable = isNullableMetadata == null || isNullableMetadata.equals("YES");
                    } catch (Exception e) {
                        log.debug("Failed to fetch nullable flag for {}.{}", table, column.getName(), e);
                    }
                    column.setNullable(nullable);

                    boolean primaryKey = false;
                    try {
                        primaryKey = primaryKeyNames.stream().filter(pk -> pk.equals(columnName(rs))).count() > 0;
                    } catch (Exception e) {
                        log.debug("Failed to fetch primary key or not for {}.{}", table, column.getName(), e);
                    }
                    column.setPrimaryKey(primaryKey);

                    // linweiyu 的处理
                    {
                        // COLUMN_SIZE int => 列的大小。对于 char 或 date 类型，列的大小是最大字符数，对于 numeric 和 decimal 类型，列的大小就是精度
                        {
                            column.setColumnSize(rs.getInt("COLUMN_SIZE"));
                        }
                    }

                    tableInfo.getColumns().add(column);
                }
            }
            return tableInfo;

        } finally {
            databaseMeta.getConnection().close();
        }
    }

    private static final String columnName(ResultSet rs) {
        try {
            return rs.getString("COLUMN_NAME");
        } catch (SQLException e) {
            return null;
        }
    }

    private static String extractSchema(String schemaAndTable) {
        if (schemaAndTable.contains(".")) {
            return schemaAndTable.split(".")[0];
        } else {
            return null;
        }
    }

    private static String extractTabeName(String schemaAndTable) {
        if (schemaAndTable.contains(".")) {
            return schemaAndTable.split(".")[1];
        } else {
            return schemaAndTable;
        }
    }

}
