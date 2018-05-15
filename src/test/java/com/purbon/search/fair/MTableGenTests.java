package com.purbon.search.fair;

import com.purbon.search.fair.utils.MTableGenerator;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.common.io.PathUtils;
import org.junit.Before;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MTableGenTests extends LuceneTestCase {

    private int[] mtable40_06_01;
    private int[] mtable50_03_03;
    private int[] mtable500_05_001;
    private int[] mtable50_04_01;
    private int[] mtable50_04_02;
    private int[] mtable50_05_01;
    private int[] mtable50_05_02;
    private int[] mtable50_06_01;
    private int[] mtable50_06_02;
    private int[] mtable100_04_01;
    private int[] mtable100_04_02;
    private int[] mtable100_05_01;
    private int[] mtable100_05_02;
    private int[] mtable100_06_01;
    private int[] mtable100_06_02;
    private int[] mtable200_04_01;
    private int[] mtable200_04_02;
    private int[] mtable200_05_01;
    private int[] mtable200_05_02;
    private int[] mtable200_06_01;
    private int[] mtable200_06_02;

    @Before
    public void setup() throws IOException, URISyntaxException {
        //k=40,p=0.6,a=0.1
        this.mtable40_06_01 = loadMTableFixture("mtable40_06_01.dat");
        //k=50, p=0.5, a=0.3
        this.mtable50_03_03 = loadMTableFixture("mtable50_05_03.dat");
        //k=500, p=0.5, a=0.01
        this.mtable500_05_001 = loadMTableFixture("mtable500_05_001.dat");

        this.mtable50_04_01 = loadMTableFixture("mtable50_04_01.dat");
        this.mtable50_04_02 = loadMTableFixture("mtable50_04_02.dat");
        this.mtable50_05_01 = loadMTableFixture("mtable50_05_01.dat");
        this.mtable50_05_02 = loadMTableFixture("mtable50_05_02.dat");
        this.mtable50_06_01 = loadMTableFixture("mtable50_06_01.dat");
        this.mtable50_06_02 = loadMTableFixture("mtable50_06_02.dat");

        this.mtable100_04_01 = loadMTableFixture("mtable100_04_01.dat");
        this.mtable100_04_02 = loadMTableFixture("mtable100_04_02.dat");
        this.mtable100_05_01 = loadMTableFixture("mtable100_05_01.dat");
        this.mtable100_05_02 = loadMTableFixture("mtable100_05_02.dat");
        this.mtable100_06_01 = loadMTableFixture("mtable100_06_01.dat");
        this.mtable100_06_02 = loadMTableFixture("mtable100_06_02.dat");

        this.mtable200_04_01 = loadMTableFixture("mtable200_04_01.dat");
        this.mtable200_04_02 = loadMTableFixture("mtable200_04_02.dat");
        this.mtable200_05_01 = loadMTableFixture("mtable200_05_01.dat");
        this.mtable200_05_02 = loadMTableFixture("mtable200_05_02.dat");
        this.mtable200_06_01 = loadMTableFixture("mtable200_06_01.dat");
        this.mtable200_06_02 = loadMTableFixture("mtable200_06_02.dat");
    }

    public void testComputeMTableWithValidParametersTest() {

        MTableGenerator gen1 = new MTableGenerator(40, 0.6, 0.1, true);
        MTableGenerator gen2 = new MTableGenerator(50, 0.5, 0.3, true);
        MTableGenerator gen3 = new MTableGenerator(500, 0.5, 0.01, true);

        MTableGenerator gen4 = new MTableGenerator(50, 0.4, 0.1, true);
        MTableGenerator gen5 = new MTableGenerator(50, 0.4, 0.2, true);
        MTableGenerator gen6 = new MTableGenerator(50, 0.5, 0.1, true);
        MTableGenerator gen7 = new MTableGenerator(50, 0.5, 0.2, true);
        MTableGenerator gen8 = new MTableGenerator(50, 0.6, 0.1, true);
        MTableGenerator gen9 = new MTableGenerator(50, 0.6, 0.2, true);

        MTableGenerator gen10 = new MTableGenerator(100, 0.4, 0.1, true);
        MTableGenerator gen11 = new MTableGenerator(100, 0.4, 0.2, true);
        MTableGenerator gen12 = new MTableGenerator(100, 0.5, 0.1, true);
        MTableGenerator gen13 = new MTableGenerator(100, 0.5, 0.2, true);
        MTableGenerator gen14 = new MTableGenerator(100, 0.6, 0.1, true);
        MTableGenerator gen15 = new MTableGenerator(100, 0.6, 0.2, true);

        MTableGenerator gen16 = new MTableGenerator(200, 0.4, 0.1, true);
        MTableGenerator gen17 = new MTableGenerator(200, 0.4, 0.2, true);
        MTableGenerator gen18 = new MTableGenerator(200, 0.5, 0.1, true);
        MTableGenerator gen19 = new MTableGenerator(200, 0.5, 0.2, true);
        MTableGenerator gen20 = new MTableGenerator(200, 0.6, 0.1, true);
        MTableGenerator gen21 = new MTableGenerator(200, 0.6, 0.2, true);

        boolean gen1MatchesMTable40_06_01 = false;
        boolean gen2MatchesMTable50_03_03 = false;
        boolean gen3MatchesMTable500_05_001 = false;

        boolean gen4MatchesMtable50_04_01 = false;
        boolean gen5MatchesMtable50_04_02 = false;
        boolean gen6MatchesMtable50_05_01 = false;
        boolean gen7MatchesMtable50_05_02 = false;
        boolean gen8MatchesMtable50_06_01 = false;
        boolean gen9MatchesMtable50_06_02 = false;

        boolean gen10MatchesMtable100_04_01 = false;
        boolean gen11MatchesMtable100_04_02 = false;
        boolean gen12MatchesMtable100_05_01 = false;
        boolean gen13MatchesMtable100_05_02 = false;
        boolean gen14MatchesMtable100_06_01 = false;
        boolean gen15MatchesMtable100_06_02 = false;

        boolean gen16MatchesMtable200_04_01 = false;
        boolean gen17MatchesMtable200_04_02 = false;
        boolean gen18MatchesMtable200_05_01 = false;
        boolean gen19MatchesMtable200_05_02 = false;
        boolean gen20MatchesMtable200_06_01 = false;
        boolean gen21MatchesMtable200_06_02 = false;

        int[] gen1MTable = gen1.getMTable();
        int[] gen2MTable = gen2.getMTable();
        int[] gen3MTable = gen3.getMTable();
        int[] gen4MTable = gen4.getMTable();
        int[] gen5MTable = gen5.getMTable();
        int[] gen6MTable = gen6.getMTable();
        int[] gen7MTable = gen7.getMTable();
        int[] gen8MTable = gen8.getMTable();
        int[] gen9MTable = gen9.getMTable();
        int[] gen10MTable = gen10.getMTable();
        int[] gen11MTable = gen11.getMTable();
        int[] gen12MTable = gen12.getMTable();
        int[] gen13MTable = gen13.getMTable();
        int[] gen14MTable = gen14.getMTable();
        int[] gen15MTable = gen15.getMTable();
        int[] gen16MTable = gen16.getMTable();
        int[] gen17MTable = gen17.getMTable();
        int[] gen18MTable = gen18.getMTable();
        int[] gen19MTable = gen19.getMTable();
        int[] gen20MTable = gen20.getMTable();
        int[] gen21MTable = gen21.getMTable();

        gen1MatchesMTable40_06_01 = arraysAreEqual(gen1MTable, mtable40_06_01);
        gen2MatchesMTable50_03_03 = arraysAreEqual(gen2MTable, mtable50_03_03);
        gen3MatchesMTable500_05_001 = arraysAreEqual(gen3MTable, mtable500_05_001);

        boolean b1 = gen1MatchesMTable40_06_01 && gen2MatchesMTable50_03_03 && gen3MatchesMTable500_05_001;

        gen4MatchesMtable50_04_01 = arraysAreEqual(gen4MTable, mtable50_04_01);
        gen5MatchesMtable50_04_02 = arraysAreEqual(gen5MTable, mtable50_04_02);
        gen6MatchesMtable50_05_01 = arraysAreEqual(gen6MTable, mtable50_05_01);
        gen7MatchesMtable50_05_02 = arraysAreEqual(gen7MTable, mtable50_05_02);
        gen8MatchesMtable50_06_01 = arraysAreEqual(gen8MTable, mtable50_06_01);
        gen9MatchesMtable50_06_02 = arraysAreEqual(gen9MTable, mtable50_06_02);

        boolean b2 = gen4MatchesMtable50_04_01 && gen5MatchesMtable50_04_02 && gen6MatchesMtable50_05_01 && gen7MatchesMtable50_05_02 && gen8MatchesMtable50_06_01 && gen9MatchesMtable50_06_02;

        gen10MatchesMtable100_04_01 = arraysAreEqual(gen10MTable, mtable100_04_01);
        gen11MatchesMtable100_04_02 = arraysAreEqual(gen11MTable, mtable100_04_02);
        gen12MatchesMtable100_05_01 = arraysAreEqual(gen12MTable, mtable100_05_01);
        gen13MatchesMtable100_05_02 = arraysAreEqual(gen13MTable, mtable100_05_02);
        gen14MatchesMtable100_06_01 = arraysAreEqual(gen14MTable, mtable100_06_01);
        gen15MatchesMtable100_06_02 = arraysAreEqual(gen15MTable, mtable100_06_02);

        boolean b3 = gen10MatchesMtable100_04_01 && gen11MatchesMtable100_04_02 && gen12MatchesMtable100_05_01 && gen13MatchesMtable100_05_02 && gen14MatchesMtable100_06_01 && gen15MatchesMtable100_06_02;

        gen16MatchesMtable200_04_01 = arraysAreEqual(gen16MTable, mtable200_04_01);
        gen17MatchesMtable200_04_02 = arraysAreEqual(gen17MTable, mtable200_04_02);
        gen18MatchesMtable200_05_01 = arraysAreEqual(gen18MTable, mtable200_05_01);
        gen19MatchesMtable200_05_02 = arraysAreEqual(gen19MTable, mtable200_05_02);
        gen20MatchesMtable200_06_01 = arraysAreEqual(gen20MTable, mtable200_06_01);
        gen21MatchesMtable200_06_02 = arraysAreEqual(gen21MTable, mtable200_06_02);

        boolean b4 = gen16MatchesMtable200_04_01 && gen17MatchesMtable200_04_02 && gen18MatchesMtable200_05_01 && gen19MatchesMtable200_05_02 && gen20MatchesMtable200_06_01 && gen21MatchesMtable200_06_02;

        assertTrue(b1 && b2 && b3 && b4);
    }


    public void testInitializeWithToSmallAlphaTest(){
        try{
            MTableGenerator gen = new MTableGenerator(50,0.5,0.0001, true);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testInitializeWithInvalidNValueTest() {
        try {
            MTableGenerator gen = new MTableGenerator(0, 0.5, 0.1, true);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testInitializeWithInvalidPValueTest() {
        try {

            MTableGenerator gen = new MTableGenerator(40, 1.1, 0.1, true);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testInitializeWithInvalidAlphaValueTest() {
        try {
            MTableGenerator gen = new MTableGenerator(40, 0.5, 1, true);
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

        URL file = getClass().getResource("/mtable_fixtures/" + filename);
        String text = new String(Files.readAllBytes(PathUtils.get(file.toURI())), StandardCharsets.UTF_8);

        List<String> list = Arrays.asList(text.split(","));
        return list.stream().mapToInt(i -> Integer.parseInt(i.trim())).toArray();
    }


}
