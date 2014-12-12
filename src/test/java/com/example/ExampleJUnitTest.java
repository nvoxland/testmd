package com.example;

import org.junit.Rule;
import org.junit.Test;
import testmd.Permutation;
import testmd.junit.TestMDRule;

import static org.junit.Assert.assertTrue;

public class ExampleJUnitTest {

    @Rule
    public TestMDRule testmd = new TestMDRule();

    @Test
    public void numbersAreLessThan10() throws Exception {
        Object[][] numberAndWeights = new Object[][]{new Object[]{1, 0.3}, new Object[]{2, 0.4}, new Object[]{3, 0.5}, new Object[]{4, 0.6}};

        for (Object[] permutation : numberAndWeights) {
            final int number = (Integer) permutation[0];
            final double weight = (Double) permutation[1];

            testmd.permutation()
                    .addParameter("number", number)
                    .addParameter("weight", weight)
                    .addResult("multiple", number * 63)
                    .addResult("weighted", number * weight)
                    .run(new Permutation.Verification() {
                        @Override
                        public void run() {
                            assertTrue(number < 10);
                        }
                    });
        }
    }

    @Test
    public void numbersAreMoreThan10AsATable() throws Exception {
        Object[][] numberAndWeights = new Object[][]{new Object[]{11, 0.4}, new Object[]{20, 0.3}, new Object[]{33, 0.23}, new Object[]{21, 0.3}, new Object[]{11, 0.4}, new Object[]{22, 0.3}};

        for (Object[] permutation : numberAndWeights) {
            final int number = (Integer) permutation[0];
            final double weight = (Double) permutation[1];

            testmd.permutation().addParameter("number", number)
                    .addParameter("weigh", weight)
                    .asTable("number")
                    .addResult("weighted", number * weight)
                    .run(new Permutation.Verification() {
                        @Override
                        public void run() {
                            assertTrue(number > 10);
                        }
                    });
        }
    }
}
