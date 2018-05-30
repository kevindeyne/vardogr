package com.kevindeyne.datascrambler;

import com.kevindeyne.datascrambler.domain.Table;
import com.kevindeyne.datascrambler.helper.FKMapping;
import com.kevindeyne.datascrambler.helper.Scrambler;
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
public class ScramblerTest {

    @Test
    public void testRandomWord() {
        for (int i = 0; i < 10000; i++) {
            testRandomWord((int)(Math.random() * 100) + 1);
        }
    }

    private void testRandomWord(int lengthMax) {
        String text = Scrambler.getRandomText(lengthMax);
        System.out.println(text);
        System.out.println(text.length() + " < " + lengthMax);
        System.out.println("---");
        Assert.assertTrue(text.length() <= lengthMax);
    }
}