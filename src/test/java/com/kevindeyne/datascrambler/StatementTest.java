package com.kevindeyne.datascrambler;

import com.kevindeyne.datascrambler.domain.Table;
import com.kevindeyne.datascrambler.helper.FKMapping;
import com.kevindeyne.datascrambler.helper.StatementBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@RunWith(MockitoJUnitRunner.class)
public class StatementTest {

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData meta;

    @Test
    public void testBuildStatement() throws SQLException {
        FKMapping fk = new FKMapping();

        fk.addTable("person", "id");
        fk.addDependency("person", "adres_id", "adres", "id", 0L, 0L);

        fk.addTable("post_code", "id");

        fk.addTable("order", "id");
        fk.addDependency("order", "person_id", "person", "id", 0L, 0L);
        fk.addDependency("order", "adres_id", "adres", "id", 0L, 0L);

        fk.addTable("adres", "id");
        fk.addDependency("adres", "postcode_id", "post_code", "id", 0L, 0L);

        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("post_code", fk.next().getName());
        Assert.assertTrue(fk.hasNext()); //adres

        Mockito.when(meta.getColumnCount()).thenReturn(2); //id and ref
        Mockito.when(meta.getColumnName(1)).thenReturn("id");
        Mockito.when(meta.getColumnTypeName(1)).thenReturn("int");
        Mockito.when(meta.getPrecision(1)).thenReturn(32);

        Mockito.when(meta.getColumnName(2)).thenReturn("postcode_id");
        Mockito.when(meta.getColumnTypeName(2)).thenReturn("int");
        Mockito.when(meta.getPrecision(2)).thenReturn(32);

        Mockito.when(resultSet.getMetaData()).thenReturn(meta);

        String sql = StatementBuilder.buildCreateTableStatement(fk.next(), resultSet, "TEST_DB");
        System.out.println(sql);
        Assert.assertTrue(sql.startsWith("CREATE TABLE `TEST_DB`.`adres` (`id` int(32),`postcode_id` int(32),PRIMARY KEY (`id`))"));
    }

    @Test
    public void testInsertString() throws SQLException {
        FKMapping fk = new FKMapping();

        fk.addTable("person", "id");
        fk.addDependency("person", "adres_id", "adres", "id", 0L, 0L);

        fk.addTable("post_code", "id");

        fk.addTable("order", "id");
        fk.addDependency("order", "person_id", "person", "id", 0L, 0L);
        fk.addDependency("order", "adres_id", "adres", "id", 0L, 0L);

        fk.addTable("adres", "id");
        fk.addDependency("adres", "postcode_id", "post_code", "id", 0L, 0L);

        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("post_code", fk.next().getName());
        Assert.assertTrue(fk.hasNext()); //adres

        Mockito.when(meta.getColumnCount()).thenReturn(2); //id and ref
        Mockito.when(meta.getColumnName(1)).thenReturn("id");
        Mockito.when(meta.getColumnClassName(1)).thenReturn(String.class.getName());

        Mockito.when(meta.getColumnName(2)).thenReturn("postcode_id");
        Mockito.when(meta.getColumnClassName(2)).thenReturn(Integer.class.getName());

        Mockito.when(resultSet.getMetaData()).thenReturn(meta);

        String sql = StatementBuilder.buildInsertStatement(fk.next(), "TEST_DB", resultSet);
        System.out.println(sql);
        Assert.assertTrue(sql.startsWith("INSERT INTO `TEST_DB`.`adres` VALUES ('"));
    }

    @Test
    public void testInsertInteger() throws SQLException {
        FKMapping fk = new FKMapping();

        fk.addTable("person", "id");
        fk.addDependency("person", "adres_id", "adres", "id", 0L, 0L);

        fk.addTable("post_code", "id");

        fk.addTable("order", "id");
        fk.addDependency("order", "person_id", "person", "id", 0L, 0L);
        fk.addDependency("order", "adres_id", "adres", "id", 0L, 0L);

        fk.addTable("adres", "id");
        fk.addDependency("adres", "postcode_id", "post_code", "id", 0L, 0L);

        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("post_code", fk.next().getName());
        Assert.assertTrue(fk.hasNext()); //adres

        Mockito.when(meta.getColumnCount()).thenReturn(2); //id and ref
        Mockito.when(meta.getColumnName(1)).thenReturn("id");
        Mockito.when(meta.getColumnClassName(1)).thenReturn(Integer.class.getName());

        Mockito.when(meta.getColumnName(2)).thenReturn("postcode_id");
        Mockito.when(meta.getColumnClassName(2)).thenReturn(Integer.class.getName());

        Mockito.when(resultSet.getMetaData()).thenReturn(meta);

        String sql = StatementBuilder.buildInsertStatement(fk.next(), "TEST_DB", resultSet);
        System.out.println(sql);
        Assert.assertTrue(sql.startsWith("INSERT INTO `TEST_DB`.`adres` VALUES ("));
        Assert.assertNotEquals(sql.replace("INSERT INTO `TEST_DB`.`adres` VALUES (", "").substring(0,1), "'");
    }

    @Test
    public void testInsertDate() throws SQLException {
        FKMapping fk = new FKMapping();

        fk.addTable("person", "id");
        fk.addDependency("person", "adres_id", "adres", "id", 0L, 0L);

        fk.addTable("post_code", "id");

        fk.addTable("order", "id");
        fk.addDependency("order", "person_id", "person", "id", 0L, 0L);
        fk.addDependency("order", "adres_id", "adres", "id", 0L, 0L);

        fk.addTable("adres", "id");
        fk.addDependency("adres", "postcode_id", "post_code", "id", 0L, 0L);

        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("post_code", fk.next().getName());
        Assert.assertTrue(fk.hasNext()); //adres

        Mockito.when(meta.getColumnCount()).thenReturn(1);
        Mockito.when(meta.getColumnName(1)).thenReturn("date");
        Mockito.when(meta.getColumnClassName(1)).thenReturn(Date.class.getName());
        Mockito.when(meta.getPrecision(1)).thenReturn(10);

        Mockito.when(resultSet.getMetaData()).thenReturn(meta);

        String sql = StatementBuilder.buildInsertStatement(fk.next(), "TEST_DB", resultSet);
        System.out.println(sql);
        Assert.assertTrue(sql.startsWith("INSERT INTO `TEST_DB`.`adres` VALUES ('"));
    }

    @Test
    public void testIDConsistency() throws SQLException {
        FKMapping fk = new FKMapping();

        fk.addTable("person", "id");
        fk.addDependency("person", "adres_id", "adres", "id", 0L, 0L);

        fk.addTable("post_code", "id");

        fk.addTable("order", "id");
        fk.addDependency("order", "person_id", "person", "id", 0L, 0L);
        fk.addDependency("order", "adres_id", "adres", "id", 0L, 0L);

        fk.addTable("adres", "id");
        fk.addDependency("adres", "postcode_id", "post_code", "id", 0L, 0L);

        Table postcode = fk.next();
        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("post_code", postcode.getName());

        Mockito.when(meta.getColumnCount()).thenReturn(1); //id and ref
        Mockito.when(meta.getColumnName(1)).thenReturn("id");
        Mockito.when(meta.getColumnClassName(1)).thenReturn(Integer.class.getName());
        Mockito.when(resultSet.getMetaData()).thenReturn(meta);

        String sql = StatementBuilder.buildInsertStatement(postcode, "DB", resultSet);

        Assert.assertTrue(fk.hasNext());
        Table adres = fk.next();
        Assert.assertEquals("adres", adres.getName());
        Assert.assertTrue(fk.hasNext());

        Mockito.when(meta.getColumnCount()).thenReturn(2); //id and ref
        Mockito.when(meta.getColumnName(1)).thenReturn("id");
        Mockito.when(meta.getColumnClassName(1)).thenReturn(Integer.class.getName());

        Mockito.when(meta.getColumnName(2)).thenReturn("postcode_id");
        Mockito.when(meta.getColumnClassName(2)).thenReturn(Integer.class.getName());

        Mockito.when(resultSet.getMetaData()).thenReturn(meta);

        String sql2 = StatementBuilder.buildInsertStatement(adres, "DB", resultSet);

        String idA = sql.substring(sql.indexOf("(")+1, sql.indexOf(")"));
        String idB = sql2.substring(sql2.indexOf(",")+1, sql2.indexOf(")"));

        System.out.println(idA + ": " + sql);
        System.out.println(idB + ": " + sql2);

        Assert.assertEquals(idA, idB);
    }
}