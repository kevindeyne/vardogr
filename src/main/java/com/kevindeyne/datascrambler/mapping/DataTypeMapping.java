package com.kevindeyne.datascrambler.mapping;

import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.impl.SQLDataType;

public enum DataTypeMapping {

    VARCHAR("varchar", SQLDataType.VARCHAR),
    CHAR("char", SQLDataType.CHAR),
    INTEGER("integer", SQLDataType.INTEGER),
    NUMERIC("numeric", SQLDataType.NUMERIC),
    DECIMAL("decimal", SQLDataType.DECIMAL),
    DATE("date", SQLDataType.DATE),
    TIMESTAMP("timestamp", SQLDataType.TIMESTAMP),
    TIME("time", SQLDataType.TIME),
    LONGVARCHAR("longvarchar", SQLDataType.LONGVARCHAR),
    CLOB("clob", SQLDataType.CLOB),
    NVARCHAR("nvarchar", SQLDataType.NVARCHAR),
    NCHAR("nchar", SQLDataType.NCHAR),
    LONGNVARCHAR("longnvarchar", SQLDataType.LONGNVARCHAR),
    NCLOB("nclob", SQLDataType.NCLOB),
    BOOLEAN("boolean", SQLDataType.BOOLEAN),
    BIT("bit", SQLDataType.BIT),
    TINYINT("tinyint", SQLDataType.TINYINT),
    SMALLINT("smallint", SQLDataType.SMALLINT),
    BIGINT("bigint", SQLDataType.BIGINT),
    DECIMAL_INTEGER("decimal_integer", SQLDataType.DECIMAL_INTEGER),
    TINYINTUNSIGNED("tinyint unsigned", SQLDataType.TINYINTUNSIGNED),
    SMALLINTUNSIGNED("smallint unsigned", SQLDataType.SMALLINTUNSIGNED),
    INTEGERUNSIGNED("integer unsigned", SQLDataType.INTEGERUNSIGNED),
    BIGINTUNSIGNED("bigint unsigned", SQLDataType.BIGINTUNSIGNED),
    DOUBLE("double", SQLDataType.DOUBLE),
    FLOAT("float", SQLDataType.FLOAT),
    REAL("real", SQLDataType.REAL),
    INTERVAL("interval", SQLDataType.INTERVAL),
    INTERVALYEARTOMONTH("interval year to month", SQLDataType.INTERVALYEARTOMONTH),
    INTERVALDAYTOSECOND("interval day to second", SQLDataType.INTERVALDAYTOSECOND),
    LOCALDATE("date", SQLDataType.LOCALDATE),
    LOCALTIME("time", SQLDataType.LOCALTIME),
    LOCALDATETIME("timestamp", SQLDataType.LOCALDATETIME),
    OFFSETTIME("time with time zone", SQLDataType.OFFSETTIME),
    OFFSETDATETIME("timestamp with time zone", SQLDataType.OFFSETDATETIME),

    //POSTGRESS
    INT2("int2", SQLDataType.SMALLINT),
    INT( "int", SQLDataType.INTEGER),
    INT4 ("int4", SQLDataType.INTEGER),
    INT8("int8", SQLDataType.BIGINT),
    DOUBLEPRECISION ("double precision", SQLDataType.DOUBLE),
    FLOAT8("float8", SQLDataType.FLOAT),
    FLOAT4("float4", SQLDataType.REAL),
    BOOL("bool", SQLDataType.BOOLEAN),
    CHARACTERVARYING ("character varying", SQLDataType.VARCHAR),
    CHARACTER("character", SQLDataType.CHAR),
    TEXT("text", SQLDataType.VARCHAR),//*
    TIMEWITHOUTTIMEZONE("time without time zone", SQLDataType.TIME),
    TIMEWITHTIMEZONE( "time with time zone", SQLDataType.TIMEWITHTIMEZONE),
    TIMETZ("timetz", SQLDataType.TIMEWITHTIMEZONE),
    TIMESTAMPWITHOUTTIMEZONE ("timestamp without time zone", SQLDataType.TIMESTAMP),
    TIMESTAMPWITHTIMEZONE("timestamp with time zone", SQLDataType.TIMESTAMPWITHTIMEZONE),
    TIMESTAMPTZ("timestamptz", SQLDataType.TIMESTAMPWITHTIMEZONE),
    INSTANT("timestamp with time zone", SQLDataType.INSTANT),
    BYTEA ("bytea", SQLDataType.VARCHAR),//*
    SERIAL ("serial", SQLDataType.INTEGER),
    SERIAL4 ("serial4", SQLDataType.INTEGER),
    BIGSERIAL("bigserial", SQLDataType.BIGINT),
    SERIAL8 ("serial8", SQLDataType.BIGINT ),
    BITVARYING ("bit varying", SQLDataType.VARCHAR),
    VARBIT ("varbit", SQLDataType.VARCHAR),
    BPCHAR ("bpchar", SQLDataType.CHAR ),
    REFCURSOR ("refcursor", SQLDataType.RESULT),
    UUID ("uuid", SQLDataType.UUID),
    JSON("json", SQLDataType.JSON),
    JSONB ("jsonb", SQLDataType.JSONB),
    OID( "oid", SQLDataType.BIGINT),
    OIDVECTOR( "oidvector", SQLDataType.BIGINT),
    XID ("xid", SQLDataType.BIGINT),
    TID ("tid", SQLDataType.BIGINT),
    CID ("cid", SQLDataType.BIGINT),
    ACLITEM ("aclitem", SQLDataType.VARCHAR),
    NAME ("name", SQLDataType.VARCHAR),
    REGPROC ( "regproc", SQLDataType.VARCHAR);

    private String key;
    private DataType<?> dataType;

    DataTypeMapping(String key, DataType<?> dataType) {
        this.key = key;
        this.dataType = dataType;
    }

    public static DataTypeMapping findByKey(String key) {
        for (DataTypeMapping mapping : values()) {
            if (key.equals(mapping.key)) return mapping;
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
    public static final DataType<String> LONGVARCHAR("longvarchar");
    public static final DataType<String> CLOB("clob");
    public static final DataType<String> NVARCHAR("nvarchar");
    public static final DataType<String> NCHAR("nchar");
    public static final DataType<String> LONGNVARCHAR("longnvarchar");
    public static final DataType<String> NCLOB("nclob");
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
