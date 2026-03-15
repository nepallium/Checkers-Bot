//package NeuralNet;
//
//import model.NeuralNet;
//import model.PolicyValue;
//import game.Board;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.junit.Assert.*;
//
//public class NeuralNetTest {
//
//    private static final int NUM_FEATURE_MAPS = 8;   // keep small for tests
//    private NeuralNet net;
//
//    @Before
//    public void setUp() {
//        net = new NeuralNet(NUM_FEATURE_MAPS);
//    }
//
//    private Board makeEmptyBoard() {
//        Board b = new Board();
//        b.cells = new int[8][8];
//        return b;
//    }
//
//    private Board makeSimpleBoardA() {
//        Board b = makeEmptyBoard();
//        b.cells[2][3] = 1;   // ally man
//        b.cells[5][4] = -1;  // opponent man
//        b.cells[0][7] = 2;   // ally king
//        return b;
//    }
//
//    private Board makeSimpleBoardB() {
//        Board b = makeEmptyBoard();
//        b.cells[2][3] = 1;   // ally man stays
//        b.cells[5][4] = 0;   // remove opponent man
//        b.cells[4][5] = -1;  // move opponent man elsewhere
//        b.cells[7][0] = -2;  // opponent king
//        return b;
//    }
//
//    private static void assertAllFinite(double[] arr) {
//        assertNotNull(arr);
//        for (double v : arr) {
//            assertFalse("Found NaN", Double.isNaN(v));
//            assertFalse("Found Infinity", Double.isInfinite(v));
//        }
//    }
//
//    private static void assertAllNonNegative(double[] arr) {
//        for (double v : arr) {
//            assertTrue("Expected non-negative but got " + v, v >= 0.0);
//        }
//    }
//
//    private static double sum(double[] arr) {
//        double s = 0.0;
//        for (double v : arr) s += v;
//        return s;
//    }
//
//    private static boolean arraysExactlyEqual(double[] a, double[] b) {
//        if (a == null || b == null) return false;
//        if (a.length != b.length) return false;
//        for (int i = 0; i < a.length; i++) {
//            if (a[i] != b[i]) return false;
//        }
//        return true;
//    }
//
//    @Test
//    public void forward_returnsPolicyAndValue_withCorrectShapes() {
//        Board b = makeSimpleBoardA();
//        PolicyValue pv = net.forward(b);
//
//        assertNotNull("PolicyValue should not be null", pv);
//        assertNotNull("Policy array should not be null", pv.policy);
//        assertEquals("Policy length should equal numActions", NUM_ACTIONS, pv.policy.length);
//        // value is scalar, just ensure it's finite
//        assertFalse("Value is NaN", Double.isNaN(pv.value));
//        assertFalse("Value is Infinite", Double.isInfinite(pv.value));
//    }
//
//    @Test
//    public void forward_policyIsProbabilityDistribution() {
//        Board b = makeSimpleBoardA();
//        PolicyValue pv = net.forward(b);
//
//        // Probabilities should be finite, non-negative, and sum ~ 1
//        assertAllFinite(pv.policy);
//        assertAllNonNegative(pv.policy);
//
//        double s = sum(pv.policy);
//        assertEquals("Softmax probs should sum to 1 (within tolerance)", 1.0, s, 1e-6);
//
//        // Optional: none should be absurdly tiny/huge due to NaNs/infs
//        // (not strictly required but catches some numeric bugs)
//        for (double p : pv.policy) {
//            assertTrue("Probability should be <= 1, got " + p, p <= 1.0);
//        }
//    }
//
//    @Test
//    public void forward_valueIsInRangeMinus1To1() {
//        Board b = makeSimpleBoardA();
//        PolicyValue pv = net.forward(b);
//
//        assertTrue("Value should be >= -1", pv.value >= -1.0);
//        assertTrue("Value should be <= 1", pv.value <= 1.0);
//    }
//
//    @Test
//    public void forward_differentBoardsProduceDifferentOutputs() {
//        Board b1 = makeSimpleBoardA();
//        Board b2 = makeSimpleBoardB();
//
//        PolicyValue pv1 = net.forward(b1);
//        PolicyValue pv2 = net.forward(b2);
//
//        // Extremely likely to differ if board encoding is actually used.
//        boolean policySame = arraysExactlyEqual(pv1.policy, pv2.policy);
//        boolean valueSame = (pv1.value == pv2.value);
//
//        assertFalse(
//                "Expected different outputs for different boards. " +
//                        "If this fails, you might still be feeding an all-zero board into the CNN.",
//                policySame && valueSame
//        );
//    }
//
//    @Test
//    public void forward_emptyBoardStillProducesFiniteOutputs() {
//        Board empty = makeEmptyBoard();
//        PolicyValue pv = net.forward(empty);
//
//        assertNotNull(pv);
//        assertNotNull(pv.policy);
//        assertEquals(NUM_ACTIONS, pv.policy.length);
//
//        assertAllFinite(pv.policy);
//        assertAllNonNegative(pv.policy);
//        assertEquals(1.0, sum(pv.policy), 1e-6);
//
//        assertFalse(Double.isNaN(pv.value));
//        assertFalse(Double.isInfinite(pv.value));
//        assertTrue(pv.value >= -1.0 && pv.value <= 1.0);
//    }
//}
