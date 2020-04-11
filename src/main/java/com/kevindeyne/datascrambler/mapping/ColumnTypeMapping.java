package com.kevindeyne.datascrambler.mapping;

import org.jooq.DataType;
import org.jooq.impl.SQLDataType;

import java.math.BigDecimal;

public enum ColumnTypeMapping {

    BIT(-7, SQLDataType.BOOLEAN),
	TINYINT(-6, SQLDataType.TINYINT),
	BIGINT(-5, SQLDataType.BIGINT),
	LONGVARBINARY(-4, SQLDataType.BINARY),
	VARBINARY(-3, SQLDataType.VARBINARY),
	BINARY(-2, SQLDataType.BINARY),
	LONGVARCHAR(-1, SQLDataType.LONGVARCHAR),
	NULL(0, SQLDataType.VARCHAR),
	CHAR(1, SQLDataType.CHAR),
	NUMERIC(2, SQLDataType.NUMERIC),
	DECIMAL(3, SQLDataType.DECIMAL),
	INTEGER(4, SQLDataType.INTEGER),
	SMALLINT(5, SQLDataType.SMALLINT),
	FLOAT(6, SQLDataType.FLOAT),
	REAL(7, SQLDataType.REAL),
	DOUBLE(8, SQLDataType.DOUBLE),
	VARCHAR(12, SQLDataType.VARCHAR),
	DATE(91, SQLDataType.DATE),
	TIME(92, SQLDataType.TIME),
	TIMESTAMP(93, SQLDataType.TIMESTAMP);

    private final Integer key;
    private final Class<?> clazz;

    ColumnTypeMapping(Integer key, DataType dataType) {
        this.key = key;
        this.clazz = dataType.getType();
    }

    public static ColumnTypeMapping findByKey(Integer key) {
        for (ColumnTypeMapping mapping : values()) {
            if (key.equals(mapping.key)) return mapping;
        }
        throw new IllegalArgumentException("Unsupported dataType provided: " + key);
    }

	public Class<?> getType() {
		return clazz;
	}
}
