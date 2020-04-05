package com.kevindeyne.datascrambler.mapping;

import org.jooq.DataType;
import org.jooq.impl.SQLDataType;

public enum DataTypeMapping {

    VARCHAR("varchar", SQLDataType.VARCHAR),
    CHAR("char", SQLDataType.CHAR),
    INTEGER("integer", SQLDataType.INTEGER),
    NUMERIC("numeric", SQLDataType.NUMERIC),
    DECIMAL("decimal", SQLDataType.DECIMAL),
    DATE( "date", SQLDataType.DATE),
    TIMESTAMP("timestamp", SQLDataType.TIMESTAMP),
    TIME("time", SQLDataType.TIME);

    private String key;
    private DataType<?> dataType;

    DataTypeMapping(String key, DataType<?> dataType) {
        this.key = key;
        this.dataType = dataType;
    }

    public static DataTypeMapping findByKey(String key) {
        for(DataTypeMapping mapping : values()) {
            if(key.equals(mapping.key)) return mapping;
        }
        throw new IllegalArgumentException("Unsupported dataType provided: " + key);
    }

    public String getKey() {
        return key;
    }

    public DataType<?> getDataType() {
        return dataType;
    }

    /*
    public static final DataType<String> LONGVARCHAR = new DefaultDataType((SQLDialect)null, String.class, "longvarchar");
    public static final DataType<String> CLOB = new DefaultDataType((SQLDialect)null, String.class, "clob");
    public static final DataType<String> NVARCHAR = new DefaultDataType((SQLDialect)null, String.class, "nvarchar");
    public static final DataType<String> NCHAR = new DefaultDataType((SQLDialect)null, String.class, "nchar");
    public static final DataType<String> LONGNVARCHAR = new DefaultDataType((SQLDialect)null, String.class, "longnvarchar");
    public static final DataType<String> NCLOB = new DefaultDataType((SQLDialect)null, String.class, "nclob");
    public static final DataType<Boolean> BOOLEAN = new DefaultDataType((SQLDialect)null, Boolean.class, "boolean");
    public static final DataType<Boolean> BIT = new DefaultDataType((SQLDialect)null, Boolean.class, "bit");
    public static final DataType<Byte> TINYINT = new DefaultDataType((SQLDialect)null, Byte.class, "tinyint");
    public static final DataType<Short> SMALLINT = new DefaultDataType((SQLDialect)null, Short.class, "smallint");
    public static final DataType<Long> BIGINT = new DefaultDataType((SQLDialect)null, Long.class, "bigint");
    public static final DataType<BigInteger> DECIMAL_INTEGER = new DefaultDataType((SQLDialect)null, BigInteger.class, "decimal_integer");
    public static final DataType<UByte> TINYINTUNSIGNED = new DefaultDataType((SQLDialect)null, UByte.class, "tinyint unsigned");
    public static final DataType<UShort> SMALLINTUNSIGNED = new DefaultDataType((SQLDialect)null, UShort.class, "smallint unsigned");
    public static final DataType<UInteger> INTEGERUNSIGNED = new DefaultDataType((SQLDialect)null, UInteger.class, "integer unsigned");
    public static final DataType<ULong> BIGINTUNSIGNED = new DefaultDataType((SQLDialect)null, ULong.class, "bigint unsigned");
    public static final DataType<Double> DOUBLE = new DefaultDataType((SQLDialect)null, Double.class, "double");
    public static final DataType<Double> FLOAT = new DefaultDataType((SQLDialect)null, Double.class, "float");
    public static final DataType<Float> REAL = new DefaultDataType((SQLDialect)null, Float.class, "real");

    public static final DataType<YearToSecond> INTERVAL = new DefaultDataType((SQLDialect)null, YearToSecond.class, "interval");
    public static final DataType<YearToMonth> INTERVALYEARTOMONTH = new DefaultDataType((SQLDialect)null, YearToMonth.class, "interval year to month");
    public static final DataType<DayToSecond> INTERVALDAYTOSECOND = new DefaultDataType((SQLDialect)null, DayToSecond.class, "interval day to second");
    public static final DataType<LocalDate> LOCALDATE = new DefaultDataType((SQLDialect)null, LocalDate.class, "date");
    public static final DataType<LocalTime> LOCALTIME = new DefaultDataType((SQLDialect)null, LocalTime.class, "time");
    public static final DataType<LocalDateTime> LOCALDATETIME = new DefaultDataType((SQLDialect)null, LocalDateTime.class, "timestamp");
    public static final DataType<OffsetTime> OFFSETTIME = new DefaultDataType((SQLDialect)null, OffsetTime.class, "time with time zone");
    public static final DataType<OffsetDateTime> OFFSETDATETIME = new DefaultDataType((SQLDialect)null, OffsetDateTime.class, "timestamp with time zone");
    public static final DataType<OffsetTime> TIMEWITHTIMEZONE;
    public static final DataType<OffsetDateTime> TIMESTAMPWITHTIMEZONE;
    public static final DataType<Instant> INSTANT;
    public static final DataType<byte[]> BINARY;
    public static final DataType<byte[]> VARBINARY;
    public static final DataType<byte[]> LONGVARBINARY;
    public static final DataType<byte[]> BLOB;
    public static final DataType<Object> OTHER;
    public static final DataType<RowId> ROWID;
    public static final DataType<Record> RECORD;
    public static final DataType<Result<Record>> RESULT;
    public static final DataType<java.util.UUID> UUID;
    public static final DataType<JSON> JSON;
    public static final DataType<JSONB> JSONB;*/

}
