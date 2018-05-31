package com.kevindeyne.datascrambler;

import com.kevindeyne.datascrambler.helper.FKMapping;
import org.junit.Assert;
import org.junit.Test;

public class ForeignKeyMappingTest {

    @Test
    public void testMapping(){
        FKMapping fk = new FKMapping();

        fk.addTable("person", "id");
        fk.addDependency("person", "adres_id", "adres", "id");

        fk.addTable("post_code", "id");

        fk.addTable("order", "id");
        fk.addDependency("order", "person_id", "person", "id");
        fk.addDependency("order", "adres_id", "adres", "id");

        fk.addTable("adres", "id");
        fk.addDependency("adres", "postcode_id", "post_code", "id");

        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("post_code", fk.next().getName());
        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("adres", fk.next().getName());
        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("person", fk.next().getName());
        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("order", fk.next().getName());
        Assert.assertFalse(fk.hasNext());
    }

    @Test
    public void testMappingRandomOrder(){
        FKMapping fk = new FKMapping();

        fk.addTable("person", "id");
        fk.addTable("post_code", "id");
        fk.addTable("order", "id");
        fk.addTable("adres", "id");

        fk.addDependency("person", "adres_id", "adres", "id");
        fk.addDependency("order", "person_id", "person", "id");
        fk.addDependency("order", "adres_id", "adres", "id");
        fk.addDependency("adres", "postcode_id", "post_code", "id");

        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("post_code", fk.next().getName());
        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("adres", fk.next().getName());
        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("person", fk.next().getName());
        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("order", fk.next().getName());
        Assert.assertFalse(fk.hasNext());
    }

    @Test
    public void testMappingRandomOrder2(){
        FKMapping fk = new FKMapping();

        fk.addTable("post_code", "id");
        fk.addTable("person", "id");
        fk.addDependency("order", "adres_id", "adres", "id");
        fk.addTable("order", "id");
        fk.addDependency("order", "person_id", "person", "id");
        fk.addDependency("adres", "postcode_id", "post_code", "id");
        fk.addDependency("person", "adres_id", "adres", "id");
        fk.addTable("adres", "id");

        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("post_code", fk.next().getName());
        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("adres", fk.next().getName());
        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("person", fk.next().getName());
        Assert.assertTrue(fk.hasNext());
        Assert.assertEquals("order", fk.next().getName());
        Assert.assertFalse(fk.hasNext());
    }
}