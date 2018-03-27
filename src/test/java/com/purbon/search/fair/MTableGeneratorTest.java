package com.purbon.search.fair;

import com.purbon.search.fair.utils.MTableGenerator;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MTableGeneratorTest {

    private int[] mtable1;
    private int[] mtable2;
    private int[] mtable3;

    @Before
    public void setup() {
        //n=80,k=40,p=0.6,a=0.1
        this.mtable1 = new int[]{0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 15, 15, 16, 16, 17, 17, 18, 18, 19, 19, 20};
        //n=100, k=50, p=0.5, a=0.3
        this.mtable2 = new int[]{0, 0, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15, 15, 16, 16, 17, 17, 18, 18, 19, 19, 20, 20, 21, 21, 22, 22, 23, 23};
        //n=1000, k=500, p=0.5, a=0.01
        this.mtable3 = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 9, 10, 10, 10, 10, 11, 11, 11, 11, 11, 12, 12, 12, 12, 13, 13, 13, 13, 14, 14, 14, 14, 15, 15, 15, 15, 16, 16, 16, 16, 17, 17, 17, 17, 17, 18, 18, 18, 18, 19, 19, 19, 19, 20, 20, 20, 20, 21, 21, 21, 21, 22, 22, 22, 22, 23, 23, 23, 23, 24, 24, 24, 24, 25, 25, 25, 25, 26, 26, 26, 26, 27, 27, 27, 27, 28, 28, 28, 28, 29, 29, 29, 29, 30, 30, 30, 30, 31, 31, 31, 32, 32, 32, 32, 33, 33, 33, 33, 34, 34, 34, 34, 35, 35, 35, 35, 36, 36, 36, 36, 37, 37, 37, 37, 38, 38, 38, 38, 39, 39, 39, 40, 40, 40, 40, 41, 41, 41, 41, 42, 42, 42, 42, 43, 43, 43, 43, 44, 44, 44, 44, 45, 45, 45, 46, 46, 46, 46, 47, 47, 47, 47, 48, 48, 48, 48, 49, 49, 49, 49, 50, 50, 50, 51, 51, 51, 51, 52, 52, 52, 52, 53, 53, 53, 53, 54, 54, 54, 54, 55, 55, 55, 56, 56, 56, 56, 57, 57, 57, 57, 58, 58, 58, 58, 59, 59, 59, 60, 60, 60, 60, 61, 61, 61, 61, 62, 62, 62, 62, 63, 63, 63, 64, 64, 64, 64, 65, 65, 65, 65, 66, 66, 66, 66, 67, 67, 67, 68, 68, 68, 68, 69, 69, 69, 69, 70, 70, 70, 71, 71, 71, 71, 72, 72, 72, 72, 73, 73, 73, 73, 74, 74, 74, 75, 75, 75, 75, 76, 76, 76, 76, 77, 77, 77, 78, 78, 78, 78, 79, 79, 79, 79, 80, 80, 80, 80, 81, 81, 81, 82, 82, 82, 82, 83, 83, 83, 83, 84, 84, 84, 85, 85, 85, 85, 86, 86, 86, 86, 87, 87, 87, 88, 88, 88, 88, 89, 89, 89, 89, 90, 90, 90, 91, 91, 91, 91, 92, 92, 92, 92, 93, 93, 93, 94, 94, 94, 94, 95, 95, 95, 95, 96, 96, 96, 97, 97, 97, 97, 98, 98, 98, 98, 99, 99, 99, 100, 100, 100, 100, 101, 101, 101, 101, 102, 102, 102, 103, 103, 103, 103, 104, 104, 104, 104, 105, 105, 105, 106, 106, 106, 106, 107, 107, 107, 107, 108, 108, 108, 109, 109, 109, 109, 110, 110, 110, 111, 111, 111, 111, 112, 112, 112, 112, 113, 113, 113, 114, 114, 114, 114, 115, 115, 115, 115, 116, 116, 116, 117, 117, 117, 117, 118, 118, 118, 118, 119, 119, 119, 120, 120, 120, 120, 121, 121, 121, 122, 122, 122, 122, 123, 123, 123, 123, 124, 124, 124, 125, 125, 125, 125, 126, 126, 126, 126};
    }

    @Test
    public void computeMTableWithValidParametersTest() {
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

    @Test(expected = InvalidArgumentException.class)
    public void initializeWithInvalidKValueTest(){
        MTableGenerator gen = new MTableGenerator(80,81,0.5,0.1);
    }

    @Test(expected = InvalidArgumentException.class)
    public void initializeWithInvalidNValueTest(){
        MTableGenerator gen = new MTableGenerator(0,1,0.5,0.1);
    }

    @Test(expected = InvalidArgumentException.class)
    public void initializeWithInvalidPValueTest(){
        MTableGenerator gen = new MTableGenerator(80,40,1.1,0.1);
    }

    @Test(expected = InvalidArgumentException.class)
    public void initializeWithInvalidAlphaValueTest(){
        MTableGenerator gen = new MTableGenerator(80,40,0.5,1);
    }

    private boolean arraysAreEqual(int[] arrayOne, int[] arrayTwo){
        boolean array1MatchesArray2 = false;
        if (arrayOne.length == arrayTwo.length){
            array1MatchesArray2 = true;
            for(int i =0; i<arrayOne.length; i++){
                array1MatchesArray2 = array1MatchesArray2 && ( arrayOne[i] == arrayTwo[i]);
            }
        }
        return array1MatchesArray2;
    }
}
