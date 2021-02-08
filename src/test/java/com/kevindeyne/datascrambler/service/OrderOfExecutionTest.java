package com.kevindeyne.datascrambler.service;

import org.jooq.Comparator;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.CustomRecord;
import org.jooq.impl.CustomTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

import static org.jooq.impl.DSL.quotedName;

public class OrderOfExecutionTest {

    private DistributionModelService service;

    @Before
    public void init() {
        service = new DistributionModelService();
    }

    @Test
    public void testOrderOfExecutionNoFKs() {

    }

    @Test
    public void testOrderOfExecutionStackedToOne() {
        List<Table<?>> tables = new ArrayList<>();

        final ExampleTable user = new ExampleTable("user");
        final ExampleTable actionlog = new ExampleTable("actionlog");
        actionlog.add("last_edited_by", "user", "id");
        actionlog.add("created_by", "user", "id");
        final ExampleTable purchase = new ExampleTable("purchase");
        purchase.add("registered_by", "user", "id");
        purchase.add("reviewed_by", "user", "id");
        final ExampleTable employee = new ExampleTable("employee");
        employee.add("recruited_by", "user", "id");

        tables.add(employee);
        tables.add(purchase);
        tables.add(actionlog);
        tables.add(user);

        List<String> map = service.determineOrderOfExecution(tables);
        Assert.assertNotNull(map);
        Assert.assertEquals(map.size(), tables.size());

        Assert.assertTrue(map.indexOf("user") < map.indexOf("actionlog"));
    }

    @Test
    public void testOrderOfExecutionStandard() {
        List<Table<?>> tables = new ArrayList<>();

        final ExampleTable address = new ExampleTable("address");
        final ExampleTable person = new ExampleTable("person");
        person.add("lives_in", "address", "id");
        final ExampleTable book = new ExampleTable("book");
        book.add("written_by", "person", "id");
        final ExampleTable library = new ExampleTable("library");
        library.add("book_id", "book", "id");
        library.add("located_at", "address", "id");

        tables.add(book);
        tables.add(person);
        tables.add(address);
        tables.add(library);

        List<String> map = service.determineOrderOfExecution(tables);
        Assert.assertNotNull(map);
        Assert.assertEquals(map.size(), tables.size());

        Assert.assertTrue(map.indexOf("address") < map.indexOf("person"));
        Assert.assertTrue(map.indexOf("person") < map.indexOf("book"));
        Assert.assertTrue(map.indexOf("book") < map.indexOf("library"));
    }

    @Test
    public void testOrderOfExecutionComplex() {
        List<Table<?>> tables = new ArrayList<>();

        final ExampleTable actor = new ExampleTable("actor");

        final ExampleTable address = new ExampleTable("address");
        address.add("city_id", "city", "city_id");

        final ExampleTable category = new ExampleTable("category");

        final ExampleTable city = new ExampleTable("city");
        city.add("country_id", "country", "country_id");

        final ExampleTable country = new ExampleTable("country");

        final ExampleTable customer = new ExampleTable("customer");
        customer.add("address_id", "address", "address_id");

        final ExampleTable example = new ExampleTable("example");

        final ExampleTable film = new ExampleTable("film");
        film.add("language_id", "language", "language_id");

        final ExampleTable film_actor = new ExampleTable("film_actor");
        film_actor.add("actor_id", "actor", "actor_id");
        film_actor.add("film_id", "film", "film_id");

        final ExampleTable film_category = new ExampleTable("film_category");
        film_category.add("category_id", "category", "category_id");
        film_category.add("film_id", "film", "film_id");

        final ExampleTable inventory = new ExampleTable("inventory");
        inventory.add("film_id", "film", "film_id");

        final ExampleTable language = new ExampleTable("language");

        final ExampleTable payment = new ExampleTable("payment");
        payment.add("customer_id", "customer", "customer_id");
        payment.add("rental_id", "rental", "rental_id");
        payment.add("staff_id", "staff", "staff_id");

        final ExampleTable rental = new ExampleTable("rental");
        rental.add("customer_id", "customer", "customer_id");
        rental.add("inventory_id", "inventory", "inventory_id");
        rental.add("staff_id", "staff", "staff_id");

        final ExampleTable staff = new ExampleTable("staff");
        staff.add("address_id", "address", "address_id");

        final ExampleTable store = new ExampleTable("store");
        store.add("address_id", "address", "address_id");
        store.add("manager_staff_id", "staff", "staff_id");

        tables.add(store);
        tables.add(staff);
        tables.add(rental);
        tables.add(payment);
        tables.add(language);
        tables.add(inventory);
        tables.add(film_category);
        tables.add(film_actor);
        tables.add(film);
        tables.add(example);
        tables.add(customer);
        tables.add(country);
        tables.add(city);
        tables.add(category);
        tables.add(address);
        tables.add(actor);

        List<String> map = service.determineOrderOfExecution(tables);
        Assert.assertNotNull(map);
        Assert.assertEquals(map.size(), tables.size());

        Assert.assertTrue(map.indexOf("city") < map.indexOf("address"));
    }

    //IGNORE - test classes
    class ExampleTable extends CustomTable<ExampleRecord> {

        List<ForeignKey<ExampleRecord, ?>> foreignKeys = new ArrayList<>();

        public ExampleTable(String name) {
            super(quotedName(name));
        }

        @Override
        public Class<? extends ExampleRecord> getRecordType() {
            return ExampleRecord.class;
        }

        @Override
        public List<ForeignKey<ExampleRecord, ?>> getReferences() {
            return foreignKeys;
        }

        public void add(String internalField, String foreignTable, String foreignField) {
            ForeignKey<ExampleRecord, ?> key = new ForeignKey<ExampleRecord, ExampleRecord>() {
                @Override
                public UniqueKey<ExampleRecord> getKey() {
                    return new ExampleKey(foreignTable, foreignField);
                }

                @Override
                public List<TableField<ExampleRecord, ?>> getKeyFields() {
                    return null;
                }

                @Override
                public TableField<ExampleRecord, ?>[] getKeyFieldsArray() {
                    return new TableField[0];
                }

                @Override
                public ExampleRecord fetchParent(ExampleRecord exampleRecord) throws DataAccessException {
                    return null;
                }

                @Override
                public Result<ExampleRecord> fetchParents(ExampleRecord... exampleRecords) throws DataAccessException {
                    return null;
                }

                @Override
                public Result<ExampleRecord> fetchParents(Collection<? extends ExampleRecord> collection) throws DataAccessException {
                    return null;
                }

                @Override
                public Result<ExampleRecord> fetchChildren(ExampleRecord record) throws DataAccessException {
                    return null;
                }

                @Override
                public Result<ExampleRecord> fetchChildren(ExampleRecord... records) throws DataAccessException {
                    return null;
                }

                @Override
                public Result<ExampleRecord> fetchChildren(Collection<? extends ExampleRecord> collection) throws DataAccessException {
                    return null;
                }

                @Override
                public Table<ExampleRecord> parent(ExampleRecord exampleRecord) {
                    return null;
                }

                @Override
                public Table<ExampleRecord> parents(ExampleRecord... exampleRecords) {
                    return null;
                }

                @Override
                public Table<ExampleRecord> parents(Collection<? extends ExampleRecord> collection) {
                    return null;
                }

                @Override
                public Table<ExampleRecord> children(ExampleRecord exampleRecord) {
                    return null;
                }

                @Override
                public Table<ExampleRecord> children(ExampleRecord... exampleRecords) {
                    return null;
                }

                @Override
                public Table<ExampleRecord> children(Collection<? extends ExampleRecord> collection) {
                    return null;
                }

                @Override
                public Table<ExampleRecord> getTable() {
                    return new ExampleTable(getName());
                }

                @Override
                public List<TableField<ExampleRecord, ?>> getFields() {
                    return Arrays.asList(new ExampleField(internalField));
                }

                @Override
                public TableField<ExampleRecord, ?>[] getFieldsArray() {
                    return new TableField[0];
                }

                @Override
                public Constraint constraint() {
                    return null;
                }

                @Override
                public boolean enforced() {
                    return true;
                }

                @Override
                public boolean nullable() {
                    return false;
                }

                @Override
                public String getName() {
                    return null;
                }

                @Override
                public Name getQualifiedName() {
                    return null;
                }

                @Override
                public Name getUnqualifiedName() {
                    return null;
                }

                @Override
                public String getComment() {
                    return null;
                }

                @Override
                public Comment getCommentPart() {
                    return null;
                }
            };
            foreignKeys.add(key);
        }
    }

    public class ExampleRecord extends CustomRecord<ExampleRecord> {
        protected ExampleRecord() {
            super(null);
        }
    }

    public class ExampleKey implements UniqueKey<ExampleRecord> {

        private final String foreignTable;
        private final String foreignField;

        public ExampleKey(String foreignTable, String foreignField) {
            this.foreignField = foreignField;
            this.foreignTable = foreignTable;
        }

        @Override
        public List<ForeignKey<?, ExampleRecord>> getReferences() {
            return null;
        }

        @Override
        public boolean isPrimary() {
            return false;
        }

        @Override
        public Table<ExampleRecord> getTable() {
            return new ExampleTable(foreignTable);
        }

        @Override
        public List<TableField<ExampleRecord, ?>> getFields() {
            return Arrays.asList(new ExampleField(foreignField));
        }

        @Override
        public TableField<ExampleRecord, ?>[] getFieldsArray() {
            return new TableField[0];
        }

        @Override
        public Constraint constraint() {
            return null;
        }

        @Override
        public boolean enforced() {
            return false;
        }

        @Override
        public boolean nullable() {
            return false;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Name getQualifiedName() {
            return null;
        }

        @Override
        public Name getUnqualifiedName() {
            return null;
        }

        @Override
        public String getComment() {
            return null;
        }

        @Override
        public Comment getCommentPart() {
            return null;
        }

        @Override
        public String toString() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    class ExampleField implements TableField<ExampleRecord, ExampleTable> {

        private final String internalField;

        public ExampleField(String internalField) {
            super();
            this.internalField = internalField;
        }

        @Override
        public Table<ExampleRecord> getTable() {
            return null;
        }

        @Override
        public String getName() {
            return internalField;
        }

        @Override
        public Name getQualifiedName() {
            return null;
        }

        @Override
        public Name getUnqualifiedName() {
            return null;
        }

        @Override
        public String getComment() {
            return null;
        }

        @Override
        public Comment getCommentPart() {
            return null;
        }

        @Override
        public Field<ExampleTable> as(String s) {
            return null;
        }

        @Override
        public Field<ExampleTable> as(Name name) {
            return null;
        }

        @Override
        public Field<ExampleTable> as(Field<?> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> as(Function<? super Field<ExampleTable>, ? extends String> function) {
            return null;
        }

        @Override
        public String toString() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public <Z> Field<Z> cast(Field<Z> field) {
            return null;
        }

        @Override
        public <Z> Field<Z> cast(DataType<Z> dataType) {
            return null;
        }

        @Override
        public <Z> Field<Z> cast(Class<Z> aClass) {
            return null;
        }

        @Override
        public <Z> Field<Z> coerce(Field<Z> field) {
            return null;
        }

        @Override
        public <Z> Field<Z> coerce(DataType<Z> dataType) {
            return null;
        }

        @Override
        public <Z> Field<Z> coerce(Class<Z> aClass) {
            return null;
        }

        @Override
        public SortField<ExampleTable> asc() {
            return null;
        }

        @Override
        public SortField<ExampleTable> desc() {
            return null;
        }

        @Override
        public SortField<ExampleTable> sortDefault() {
            return null;
        }

        @Override
        public SortField<ExampleTable> sort(SortOrder sortOrder) {
            return null;
        }

        @Override
        public SortField<Integer> sortAsc(Collection<ExampleTable> collection) {
            return null;
        }

        @Override
        public SortField<Integer> sortAsc(ExampleTable... exampleTables) {
            return null;
        }

        @Override
        public SortField<Integer> sortDesc(Collection<ExampleTable> collection) {
            return null;
        }

        @Override
        public SortField<Integer> sortDesc(ExampleTable... exampleTables) {
            return null;
        }

        @Override
        public <Z> SortField<Z> sort(Map<ExampleTable, Z> map) {
            return null;
        }

        @Override
        public Field<ExampleTable> neg() {
            return null;
        }

        @Override
        public Field<ExampleTable> unaryMinus() {
            return null;
        }

        @Override
        public Field<ExampleTable> unaryPlus() {
            return null;
        }

        @Override
        public Field<ExampleTable> add(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> add(Field<?> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> plus(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> plus(Field<?> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> sub(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> sub(Field<?> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> subtract(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> subtract(Field<?> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> minus(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> minus(Field<?> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> mul(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> mul(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> multiply(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> multiply(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> times(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> times(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> div(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> div(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> divide(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> divide(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> mod(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> mod(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> modulo(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> modulo(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> rem(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> rem(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<BigDecimal> pow(Number number) {
            return null;
        }

        @Override
        public Field<BigDecimal> pow(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<BigDecimal> power(Number number) {
            return null;
        }

        @Override
        public Field<BigDecimal> power(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitNot() {
            return null;
        }

        @Override
        public Field<ExampleTable> bitAnd(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitAnd(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitNand(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitNand(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitOr(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitOr(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitNor(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitNor(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitXor(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitXor(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitXNor(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Field<ExampleTable> bitXNor(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> shl(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> shl(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<ExampleTable> shr(Number number) {
            return null;
        }

        @Override
        public Field<ExampleTable> shr(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Condition isDocument() {
            return null;
        }

        @Override
        public Condition isNotDocument() {
            return null;
        }

        @Override
        public Condition isJson() {
            return null;
        }

        @Override
        public Condition isNotJson() {
            return null;
        }

        @Override
        public Condition isNull() {
            return null;
        }

        @Override
        public Condition isNotNull() {
            return null;
        }

        @Override
        public Condition isDistinctFrom(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition isDistinctFrom(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition isDistinctFrom(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition isNotDistinctFrom(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition isNotDistinctFrom(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition isNotDistinctFrom(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition likeRegex(String s) {
            return null;
        }

        @Override
        public Condition likeRegex(Field<String> field) {
            return null;
        }

        @Override
        public Condition notLikeRegex(String s) {
            return null;
        }

        @Override
        public Condition notLikeRegex(Field<String> field) {
            return null;
        }

        @Override
        public LikeEscapeStep similarTo(Field<String> field) {
            return null;
        }

        @Override
        public Condition similarTo(Field<String> field, char c) {
            return null;
        }

        @Override
        public LikeEscapeStep similarTo(String s) {
            return null;
        }

        @Override
        public Condition similarTo(String s, char c) {
            return null;
        }

        @Override
        public LikeEscapeStep notSimilarTo(Field<String> field) {
            return null;
        }

        @Override
        public Condition notSimilarTo(Field<String> field, char c) {
            return null;
        }

        @Override
        public LikeEscapeStep notSimilarTo(String s) {
            return null;
        }

        @Override
        public Condition notSimilarTo(String s, char c) {
            return null;
        }

        @Override
        public LikeEscapeStep like(Field<String> field) {
            return null;
        }

        @Override
        public Condition like(Field<String> field, char c) {
            return null;
        }

        @Override
        public LikeEscapeStep like(String s) {
            return null;
        }

        @Override
        public Condition like(String s, char c) {
            return null;
        }

        @Override
        public LikeEscapeStep like(QuantifiedSelect<Record1<String>> quantifiedSelect) {
            return null;
        }

        @Override
        public LikeEscapeStep likeIgnoreCase(Field<String> field) {
            return null;
        }

        @Override
        public Condition likeIgnoreCase(Field<String> field, char c) {
            return null;
        }

        @Override
        public LikeEscapeStep likeIgnoreCase(String s) {
            return null;
        }

        @Override
        public Condition likeIgnoreCase(String s, char c) {
            return null;
        }

        @Override
        public LikeEscapeStep notLike(Field<String> field) {
            return null;
        }

        @Override
        public Condition notLike(Field<String> field, char c) {
            return null;
        }

        @Override
        public LikeEscapeStep notLike(String s) {
            return null;
        }

        @Override
        public Condition notLike(String s, char c) {
            return null;
        }

        @Override
        public LikeEscapeStep notLike(QuantifiedSelect<Record1<String>> quantifiedSelect) {
            return null;
        }

        @Override
        public LikeEscapeStep notLikeIgnoreCase(Field<String> field) {
            return null;
        }

        @Override
        public Condition notLikeIgnoreCase(Field<String> field, char c) {
            return null;
        }

        @Override
        public LikeEscapeStep notLikeIgnoreCase(String s) {
            return null;
        }

        @Override
        public Condition notLikeIgnoreCase(String s, char c) {
            return null;
        }

        @Override
        public Condition contains(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition contains(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition notContains(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition notContains(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition containsIgnoreCase(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition containsIgnoreCase(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition notContainsIgnoreCase(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition notContainsIgnoreCase(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition startsWith(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition startsWith(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition startsWithIgnoreCase(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition startsWithIgnoreCase(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition endsWith(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition endsWith(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition endsWithIgnoreCase(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition endsWithIgnoreCase(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition in(Collection<?> collection) {
            return null;
        }

        @Override
        public Condition in(Result<? extends Record1<ExampleTable>> result) {
            return null;
        }

        @Override
        public Condition in(ExampleTable... exampleTables) {
            return null;
        }

        @Override
        public Condition in(Field<?>... fields) {
            return null;
        }

        @Override
        public Condition in(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition notIn(Collection<?> collection) {
            return null;
        }

        @Override
        public Condition notIn(Result<? extends Record1<ExampleTable>> result) {
            return null;
        }

        @Override
        public Condition notIn(ExampleTable... exampleTables) {
            return null;
        }

        @Override
        public Condition notIn(Field<?>... fields) {
            return null;
        }

        @Override
        public Condition notIn(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition between(ExampleTable exampleTable, ExampleTable t1) {
            return null;
        }

        @Override
        public Condition between(Field<ExampleTable> field, Field<ExampleTable> field1) {
            return null;
        }

        @Override
        public Condition betweenSymmetric(ExampleTable exampleTable, ExampleTable t1) {
            return null;
        }

        @Override
        public Condition betweenSymmetric(Field<ExampleTable> field, Field<ExampleTable> field1) {
            return null;
        }

        @Override
        public Condition notBetween(ExampleTable exampleTable, ExampleTable t1) {
            return null;
        }

        @Override
        public Condition notBetween(Field<ExampleTable> field, Field<ExampleTable> field1) {
            return null;
        }

        @Override
        public Condition notBetweenSymmetric(ExampleTable exampleTable, ExampleTable t1) {
            return null;
        }

        @Override
        public Condition notBetweenSymmetric(Field<ExampleTable> field, Field<ExampleTable> field1) {
            return null;
        }

        @Override
        public BetweenAndStep<ExampleTable> between(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public BetweenAndStep<ExampleTable> between(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public BetweenAndStep<ExampleTable> betweenSymmetric(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public BetweenAndStep<ExampleTable> betweenSymmetric(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public BetweenAndStep<ExampleTable> notBetween(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public BetweenAndStep<ExampleTable> notBetween(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public BetweenAndStep<ExampleTable> notBetweenSymmetric(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public BetweenAndStep<ExampleTable> notBetweenSymmetric(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition compare(Comparator comparator, ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition compare(Comparator comparator, Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition compare(Comparator comparator, Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition compare(Comparator comparator, QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition equal(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition equal(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition equal(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition equal(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition eq(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition eq(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition eq(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition eq(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition notEqual(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition notEqual(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition notEqual(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition notEqual(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition ne(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition ne(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition ne(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition ne(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition lessThan(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition lessThan(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition lessThan(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition lessThan(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition lt(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition lt(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition lt(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition lt(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition lessOrEqual(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition lessOrEqual(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition lessOrEqual(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition lessOrEqual(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition le(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition le(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition le(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition le(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition greaterThan(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition greaterThan(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition greaterThan(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition greaterThan(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition gt(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition gt(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition gt(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition gt(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition greaterOrEqual(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition greaterOrEqual(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition greaterOrEqual(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition greaterOrEqual(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition ge(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Condition ge(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public Condition ge(Select<? extends Record1<ExampleTable>> select) {
            return null;
        }

        @Override
        public Condition ge(QuantifiedSelect<? extends Record1<ExampleTable>> quantifiedSelect) {
            return null;
        }

        @Override
        public Condition isTrue() {
            return null;
        }

        @Override
        public Condition isFalse() {
            return null;
        }

        @Override
        public Condition equalIgnoreCase(String s) {
            return null;
        }

        @Override
        public Condition equalIgnoreCase(Field<String> field) {
            return null;
        }

        @Override
        public Condition notEqualIgnoreCase(String s) {
            return null;
        }

        @Override
        public Condition notEqualIgnoreCase(Field<String> field) {
            return null;
        }

        @Override
        public Field<Integer> sign() {
            return null;
        }

        @Override
        public Field<ExampleTable> abs() {
            return null;
        }

        @Override
        public Field<ExampleTable> round() {
            return null;
        }

        @Override
        public Field<ExampleTable> round(int i) {
            return null;
        }

        @Override
        public Field<ExampleTable> floor() {
            return null;
        }

        @Override
        public Field<ExampleTable> ceil() {
            return null;
        }

        @Override
        public Field<BigDecimal> sqrt() {
            return null;
        }

        @Override
        public Field<BigDecimal> exp() {
            return null;
        }

        @Override
        public Field<BigDecimal> ln() {
            return null;
        }

        @Override
        public Field<BigDecimal> log(int i) {
            return null;
        }

        @Override
        public Field<BigDecimal> acos() {
            return null;
        }

        @Override
        public Field<BigDecimal> asin() {
            return null;
        }

        @Override
        public Field<BigDecimal> atan() {
            return null;
        }

        @Override
        public Field<BigDecimal> atan2(Number number) {
            return null;
        }

        @Override
        public Field<BigDecimal> atan2(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<BigDecimal> cos() {
            return null;
        }

        @Override
        public Field<BigDecimal> sin() {
            return null;
        }

        @Override
        public Field<BigDecimal> tan() {
            return null;
        }

        @Override
        public Field<BigDecimal> cot() {
            return null;
        }

        @Override
        public Field<BigDecimal> sinh() {
            return null;
        }

        @Override
        public Field<BigDecimal> cosh() {
            return null;
        }

        @Override
        public Field<BigDecimal> tanh() {
            return null;
        }

        @Override
        public Field<BigDecimal> coth() {
            return null;
        }

        @Override
        public Field<BigDecimal> deg() {
            return null;
        }

        @Override
        public Field<BigDecimal> rad() {
            return null;
        }

        @Override
        public Field<Integer> count() {
            return null;
        }

        @Override
        public Field<Integer> countDistinct() {
            return null;
        }

        @Override
        public Field<ExampleTable> max() {
            return null;
        }

        @Override
        public Field<ExampleTable> min() {
            return null;
        }

        @Override
        public Field<BigDecimal> sum() {
            return null;
        }

        @Override
        public Field<BigDecimal> avg() {
            return null;
        }

        @Override
        public Field<BigDecimal> median() {
            return null;
        }

        @Override
        public Field<BigDecimal> stddevPop() {
            return null;
        }

        @Override
        public Field<BigDecimal> stddevSamp() {
            return null;
        }

        @Override
        public Field<BigDecimal> varPop() {
            return null;
        }

        @Override
        public Field<BigDecimal> varSamp() {
            return null;
        }

        @Override
        public WindowPartitionByStep<Integer> countOver() {
            return null;
        }

        @Override
        public WindowPartitionByStep<ExampleTable> maxOver() {
            return null;
        }

        @Override
        public WindowPartitionByStep<ExampleTable> minOver() {
            return null;
        }

        @Override
        public WindowPartitionByStep<BigDecimal> sumOver() {
            return null;
        }

        @Override
        public WindowPartitionByStep<BigDecimal> avgOver() {
            return null;
        }

        @Override
        public WindowIgnoreNullsStep<ExampleTable> firstValue() {
            return null;
        }

        @Override
        public WindowIgnoreNullsStep<ExampleTable> lastValue() {
            return null;
        }

        @Override
        public WindowIgnoreNullsStep<ExampleTable> lead() {
            return null;
        }

        @Override
        public WindowIgnoreNullsStep<ExampleTable> lead(int i) {
            return null;
        }

        @Override
        public WindowIgnoreNullsStep<ExampleTable> lead(int i, ExampleTable exampleTable) {
            return null;
        }

        @Override
        public WindowIgnoreNullsStep<ExampleTable> lead(int i, Field<ExampleTable> field) {
            return null;
        }

        @Override
        public WindowIgnoreNullsStep<ExampleTable> lag() {
            return null;
        }

        @Override
        public WindowIgnoreNullsStep<ExampleTable> lag(int i) {
            return null;
        }

        @Override
        public WindowIgnoreNullsStep<ExampleTable> lag(int i, ExampleTable exampleTable) {
            return null;
        }

        @Override
        public WindowIgnoreNullsStep<ExampleTable> lag(int i, Field<ExampleTable> field) {
            return null;
        }

        @Override
        public WindowPartitionByStep<BigDecimal> stddevPopOver() {
            return null;
        }

        @Override
        public WindowPartitionByStep<BigDecimal> stddevSampOver() {
            return null;
        }

        @Override
        public WindowPartitionByStep<BigDecimal> varPopOver() {
            return null;
        }

        @Override
        public WindowPartitionByStep<BigDecimal> varSampOver() {
            return null;
        }

        @Override
        public Field<String> upper() {
            return null;
        }

        @Override
        public Field<String> lower() {
            return null;
        }

        @Override
        public Field<String> trim() {
            return null;
        }

        @Override
        public Field<String> rtrim() {
            return null;
        }

        @Override
        public Field<String> ltrim() {
            return null;
        }

        @Override
        public Field<String> rpad(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<String> rpad(int i) {
            return null;
        }

        @Override
        public Field<String> rpad(Field<? extends Number> field, Field<String> field1) {
            return null;
        }

        @Override
        public Field<String> rpad(int i, char c) {
            return null;
        }

        @Override
        public Field<String> lpad(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<String> lpad(int i) {
            return null;
        }

        @Override
        public Field<String> lpad(Field<? extends Number> field, Field<String> field1) {
            return null;
        }

        @Override
        public Field<String> lpad(int i, char c) {
            return null;
        }

        @Override
        public Field<String> repeat(Number number) {
            return null;
        }

        @Override
        public Field<String> repeat(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<String> replace(Field<String> field) {
            return null;
        }

        @Override
        public Field<String> replace(String s) {
            return null;
        }

        @Override
        public Field<String> replace(Field<String> field, Field<String> field1) {
            return null;
        }

        @Override
        public Field<String> replace(String s, String s1) {
            return null;
        }

        @Override
        public Field<Integer> position(String s) {
            return null;
        }

        @Override
        public Field<Integer> position(Field<String> field) {
            return null;
        }

        @Override
        public Field<Integer> ascii() {
            return null;
        }

        @Override
        public Field<String> collate(String s) {
            return null;
        }

        @Override
        public Field<String> collate(Name name) {
            return null;
        }

        @Override
        public Field<String> collate(Collation collation) {
            return null;
        }

        @Override
        public Field<String> concat(Field<?>... fields) {
            return null;
        }

        @Override
        public Field<String> concat(String... strings) {
            return null;
        }

        @Override
        public Field<String> concat(char... chars) {
            return null;
        }

        @Override
        public Field<String> substring(int i) {
            return null;
        }

        @Override
        public Field<String> substring(Field<? extends Number> field) {
            return null;
        }

        @Override
        public Field<String> substring(int i, int i1) {
            return null;
        }

        @Override
        public Field<String> substring(Field<? extends Number> field, Field<? extends Number> field1) {
            return null;
        }

        @Override
        public Field<Integer> length() {
            return null;
        }

        @Override
        public Field<Integer> charLength() {
            return null;
        }

        @Override
        public Field<Integer> bitLength() {
            return null;
        }

        @Override
        public Field<Integer> octetLength() {
            return null;
        }

        @Override
        public Field<Integer> extract(DatePart datePart) {
            return null;
        }

        @Override
        public Field<ExampleTable> greatest(ExampleTable... exampleTables) {
            return null;
        }

        @Override
        public Field<ExampleTable> greatest(Field<?>... fields) {
            return null;
        }

        @Override
        public Field<ExampleTable> least(ExampleTable... exampleTables) {
            return null;
        }

        @Override
        public Field<ExampleTable> least(Field<?>... fields) {
            return null;
        }

        @Override
        public Field<ExampleTable> nvl(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Field<ExampleTable> nvl(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public <Z> Field<Z> nvl2(Z z, Z z1) {
            return null;
        }

        @Override
        public <Z> Field<Z> nvl2(Field<Z> field, Field<Z> field1) {
            return null;
        }

        @Override
        public Field<ExampleTable> nullif(ExampleTable exampleTable) {
            return null;
        }

        @Override
        public Field<ExampleTable> nullif(Field<ExampleTable> field) {
            return null;
        }

        @Override
        public <Z> Field<Z> decode(ExampleTable exampleTable, Z z) {
            return null;
        }

        @Override
        public <Z> Field<Z> decode(ExampleTable exampleTable, Z z, Object... objects) {
            return null;
        }

        @Override
        public <Z> Field<Z> decode(Field<ExampleTable> field, Field<Z> field1) {
            return null;
        }

        @Override
        public <Z> Field<Z> decode(Field<ExampleTable> field, Field<Z> field1, Field<?>... fields) {
            return null;
        }

        @Override
        public Field<ExampleTable> coalesce(ExampleTable exampleTable, ExampleTable... exampleTables) {
            return null;
        }

        @Override
        public Field<ExampleTable> coalesce(Field<ExampleTable> field, Field<?>... fields) {
            return null;
        }

        @Override
        public Field<ExampleTable> field(Record record) {
            return null;
        }

        @Override
        public ExampleTable get(Record record) {
            return null;
        }

        @Override
        public ExampleTable getValue(Record record) {
            return null;
        }

        @Override
        public ExampleTable original(Record record) {
            return null;
        }

        @Override
        public boolean changed(Record record) {
            return false;
        }

        @Override
        public void reset(Record record) {

        }

        @Override
        public Record1<ExampleTable> from(Record record) {
            return null;
        }

        @Override
        public Converter<?, ExampleTable> getConverter() {
            return null;
        }

        @Override
        public Binding<?, ExampleTable> getBinding() {
            return null;
        }

        @Override
        public Class<ExampleTable> getType() {
            return null;
        }

        @Override
        public DataType<ExampleTable> getDataType() {
            return null;
        }

        @Override
        public DataType<ExampleTable> getDataType(Configuration configuration) {
            return null;
        }
    }

}
