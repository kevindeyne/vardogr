package com.kevindeyne.datascrambler;

import com.kevindeyne.datascrambler.domain.Dependency;
import com.kevindeyne.datascrambler.domain.MConnection;
import com.kevindeyne.datascrambler.domain.comparator.DependencyComparator;
import com.kevindeyne.datascrambler.helper.FKMapping;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

public class ForeignKeyMappingTest {

    @Test
    public void testMapping(){
        FKMapping fk = new FKMapping();

        fk.addDependency("order", "person_id", "person", "id");
        fk.addDependency("person", "adres_id", "adres", "id");
        fk.addDependency("order", "adres_id", "adres", "id");
        fk.addDependency("adres", "postcode_id", "post_code", "id");

        List<Dependency> d = new ArrayList<>();
        d.addAll(fk.dependencyMap.values());
        d.sort(new DependencyComparator());

        System.out.println(d);

        Assert.assertEquals("post_code", d.get(0).getTable());
    }

    @Test
    public void testFullHandling() throws SQLException {
        FKMapping fk = new FKMapping();

        fk.addDependency("order", "person_id", "person", "id");
        fk.addDependency("person", "adres_id", "adres", "id");
        fk.addDependency("order", "adres_id", "adres", "id");
        fk.addDependency("adres", "postcode_id", "post_code", "id");
        fk.addTable("feestzaal", "id");

        fk.handleDependenciesWithNoChildren(new MConnection(null), "");
    }
}