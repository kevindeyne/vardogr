package com.kevindeyne.datascrambler.dao;

import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import com.kevindeyne.datascrambler.domain.distributionmodel.Generator;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import com.kevindeyne.datascrambler.mapping.DataTypeMapping;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.jooq.impl.DSL.using;

@Data
public class TargetConnectionDao {

    private final String url;
    private final String username;
    private final String password;

    public TargetConnectionDao(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public boolean testConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, username, password);
        DSLContext dsl = using(new DefaultConfiguration().derive(connection));
        Integer[] result = dsl.selectOne().fetch().intoArray(0, Integer.class);
        return result.length == 1 && result[0] == 1;
    }

    public HikariDataSource toDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(this.url);
        ds.setPassword(this.password);
        ds.setUsername(this.username);
        return ds;
    }

    public List<Table<?>> getAllTables(DataSource dataSource) {
        try (DSLContext dsl = using(new DefaultConfiguration().derive(dataSource))) {
            return dsl.meta().getTables();
        }
    }

    public void truncate(DSLContext dsl, String tableName) {
        dsl.truncate(tableName);
    }

    public void createTable(DSLContext dsl, TableData table) {
        CreateTableColumnStep createStep = null;
        try {
            createStep = dsl.createTable(DSL.table(table.getTableName()));

            for (FieldData fieldData : table.getFieldData()) {
                final Generator generator = fieldData.getGenerator();
                DataType<?> dataType = DataTypeMapping.findByKey(generator.getDataTypeKey()).getDataType();
                dataType = dataType.nullable(generator.isNullable());
                dataType = dataType.precision(generator.getPrecision());
                dataType = dataType.length(generator.getLength());

                final Field<?> field = DSL.field(fieldData.getFieldName(), dataType);
                createStep = createStep.column(field);
            }

            createStep.execute();
        } finally {
            if (createStep != null) createStep.close();
        }
    }

    public void validateTable(DSLContext dsl, TableData table) {
    }
}
