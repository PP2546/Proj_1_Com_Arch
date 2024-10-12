import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorTest {
    private Simulator.State state;

    @BeforeEach
    void setUp() {
        state = new Simulator.State();
    }

    @Test
    void testLoadMemoryFromFile() throws IOException {
        File tempFile = File.createTempFile("testInstructions", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("1048576\n1048576\n1048576\n");
        }

        assertTrue(Simulator.loadMemoryFromFile(state, tempFile.getAbsolutePath()), "Loading memory should succeed.");
        assertEquals(3, state.numMemory, "Number of loaded memory words should be 3.");
        assertEquals(1048576, state.mem[0], "First memory word should be 1048576.");
        assertEquals(1048576, state.mem[1], "Second memory word should be 1048576.");
        assertEquals(1048576, state.mem[2], "Third memory word should be 1048576.");

        tempFile.deleteOnExit(); // Clean up
    }

    @Test
    void testExecuteRFormatAddition() {
        state.reg[0] = 5;
        state.reg[1] = 10;
        state.mem[0] = (0 << 22) | (0 << 19) | (1 << 16) | (2); // ADD r0, r1, r2

        Simulator.executeRFormat(state, (a, b) -> a + b);

        assertEquals(15, state.reg[2], "The result of addition should be 15.");
    }

    @Test
    void testExecuteRFormatNAND() {
        state.reg[0] = 5;
        state.reg[1] = 10;
        state.mem[0] = (1 << 22) | (0 << 19) | (1 << 16) | (2); // NAND r0, r1, r2

        Simulator.executeRFormat(state, (a, b) -> ~(a & b));

        assertEquals(~(5 & 10), state.reg[2], "The result should match NAND operation.");
    }

    @Test
    void testExecuteLoadStoreStore() {
        state.reg[0] = 0; // base address
        state.reg[1] = 100; // value to store
        state.mem[state.pc] = (3 << 22) | (0 << 19) | (1 << 16) | 0; // SW r1, 0(r0)

        Simulator.executeLoadStore(state, false);

        assertEquals(100, state.mem[0], "Memory[0] should be 100 after storing.");
    }

    @Test
    void testExecuteBranchEqual() {
        state.reg[0] = 5;
        state.reg[1] = 5;
        state.mem[0] = (4 << 22) | (0 << 19) | (1 << 16) | 2; // BEQ r0, r1, 2

        Simulator.executeBranch(state);

        assertEquals(2, state.pc, "PC should change to 2 after branch.");
    }

    @Test
    void testExecuteJALR() {
        state.reg[0] = 4;
        state.mem[0] = (5 << 22) | (0 << 19) | (1 << 16); // JALR r0, r1

        Simulator.executeJALR(state);

        assertEquals(state.reg[0], state.pc, "PC should be set to the value of reg[0].");
    }

    @Test
    void testHalt() {
        state.mem[0] = (6 << 22);
        int initialPC = state.pc;

        Simulator.simulateMachine(state);

        assertEquals(initialPC, state.pc, "PC should remain unchanged after halt.");
    }

    @Test
    void testConvertNumEdgeCases() {
        assertEquals(-32768, Simulator.convertNum(0x8000), "Should convert 0x8000 to -32768.");
        assertEquals(32767, Simulator.convertNum(0x7FFF), "Should convert 0x7FFF to 32767.");
    }

    @Test
    void testLoadMemoryFromFile_ValidFile() throws IOException {
        String testFileName = "Output/test_valid_memory.txt";
        try (PrintWriter writer = new PrintWriter(testFileName)) {
            writer.println(5);
            writer.println(10);
            writer.println(15);
        }

        boolean result = Simulator.loadMemoryFromFile(state, testFileName);

        Assertions.assertTrue(result, "Loading from a valid file should succeed.");
        Assertions.assertEquals(3, state.numMemory, "Number of memory entries should be 3.");
        Assertions.assertArrayEquals(new int[]{5, 10, 15}, new int[]{state.mem[0], state.mem[1], state.mem[2]}, "Memory contents should match.");
    }

    @Test
    void testLoadMemoryFromFile_InvalidFile() {
        boolean result = Simulator.loadMemoryFromFile(state, "non_existent_file.txt");
        Assertions.assertFalse(result, "Loading from a non-existent file should fail.");
        Assertions.assertEquals(0, state.numMemory, "No memory should be loaded.");
    }

    @Test
    void testLoadMemoryFromFile_EmptyFile() throws IOException {
        String testFileName = "Output/test_empty_memory.txt";
        try (PrintWriter writer = new PrintWriter(testFileName)) {

        }

        boolean result = Simulator.loadMemoryFromFile(state, testFileName);
        Assertions.assertTrue(result, "Loading from an empty file should succeed.");
        Assertions.assertEquals(0, state.numMemory, "No memory should be loaded from an empty file.");
    }
}
