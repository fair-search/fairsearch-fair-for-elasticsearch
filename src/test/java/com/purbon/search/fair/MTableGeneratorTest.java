package com.purbon.search.fair;

import com.purbon.search.fair.utils.MTableGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.lang.IllegalArgumentException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.util.FileUtils;
import org.elasticsearch.common.io.FileTestUtils;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.indices.recovery.RecoveryState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MTableGeneratorTest {

    private int[] mtable1;
    private int[] mtable2;
    private int[] mtable3;


    @Before
    public void setup() throws IOException, URISyntaxException {
        //n=80,k=40,p=0.6,a=0.1
        this.mtable1 = loadMTableFixture("mtable1.dat");
        //n=100, k=50, p=0.5, a=0.3
        this.mtable2 = loadMTableFixture("mtable2.dat");
        //n=1000, k=500, p=0.5, a=0.01
        this.mtable3 = loadMTableFixture("mtable3.dat");
    }

    public void testComputeMTableWithValidParametersTest() {
        MTableGenerator gen1 = new MTableGenerator(80, 40, 0.6, 0.1);
        MTableGenerator gen2 = new MTableGenerator(100, 50, 0.5, 0.3);
        MTableGenerator gen3 = new MTableGenerator(1000, 500, 0.5, 0.01);

        boolean gen1MatchesMTable1 = false;
        boolean gen2MatchesMTable2 = false;
        boolean gen3MatchesMTable3 = false;

        int[] gen1MTable = gen1.getMTable();
        int[] gen2MTable = gen2.getMTable();
        int[] gen3MTable = gen3.getMTable();

        gen1MatchesMTable1 = arraysAreEqual(gen1MTable, mtable1);
        gen2MatchesMTable2 = arraysAreEqual(gen2MTable, mtable2);
        gen3MatchesMTable3 = arraysAreEqual(gen3MTable, mtable3);

        assertTrue(gen1MatchesMTable1 && gen2MatchesMTable2 && gen3MatchesMTable3);
    }

    public void testInitializeWithInvalidKValueTest() {
        try {
            MTableGenerator gen = new MTableGenerator(80, 81, 0.5, 0.1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testInitializeWithInvalidNValueTest() {
        try {
            MTableGenerator gen = new MTableGenerator(0, 1, 0.5, 0.1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testInitializeWithInvalidPValueTest() {
        try {
            MTableGenerator gen = new MTableGenerator(80, 40, 1.1, 0.1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testInitializeWithInvalidAlphaValueTest() {
        try {
            MTableGenerator gen = new MTableGenerator(80, 40, 0.5, 1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    private boolean arraysAreEqual(int[] arrayOne, int[] arrayTwo) {
        boolean array1MatchesArray2 = false;
        if (arrayOne.length == arrayTwo.length) {
            array1MatchesArray2 = true;
            for (int i = 0; i < arrayOne.length; i++) {
                array1MatchesArray2 = array1MatchesArray2 && (arrayOne[i] == arrayTwo[i]);
            }
        }
        return array1MatchesArray2;
    }

    private int[] loadMTableFixture(String filename) throws IOException, URISyntaxException {

        URL file = getClass().getResource("mtable_fixtures/"+filename);
        String text = new String(Files.readAllBytes(PathUtils.get(file.toURI())), StandardCharsets.UTF_8);

        List<String> list = Arrays.asList(text.split(","));
        return list.stream().mapToInt(i-> Integer.parseInt(i)).toArray();
    }


}
