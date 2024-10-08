import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorTest {
    private Simulator.State state;

    @BeforeEach
    void setUp() {
        state = new Simulator.State();
    }

    @Test
    void testLoadMemoryFromFile() throws IOException {
        // สร้างไฟล์ทดสอบ
        File tempFile = File.createTempFile("testInstructions", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("1048576\n1048576\n1048576\n"); // ตัวอย่างคำสั่งใน binary format
        }

        // ทดสอบการโหลด
        assertTrue(Simulator.loadMemoryFromFile(state, tempFile.getAbsolutePath()));
        assertEquals(3, state.numMemory);
        assertEquals(1048576, state.mem[0]);
        assertEquals(1048576, state.mem[1]);
        assertEquals(1048576, state.mem[2]);

        tempFile.deleteOnExit(); // ลบไฟล์เมื่อเสร็จ
    }

    @Test
    void testExecuteRFormatAddition() {
        // ตั้งค่าระบบเริ่มต้น
        state.reg[0] = 5;
        state.reg[1] = 10;
        state.mem[0] = (0 << 22) | (0 << 19) | (1 << 16) | (2); // ADD r0, r1, r2

        Simulator.executeRFormat(state, (a, b) -> a + b);

        assertEquals(15, state.reg[2]); // ผลลัพธ์ต้องเป็น 15
    }

    @Test
    void testExecuteRFormatNAND() {
        state.reg[0] = 5;
        state.reg[1] = 10;
        state.mem[0] = (1 << 22) | (0 << 19) | (1 << 16) | (2); // NAND r0, r1, r2

        Simulator.executeRFormat(state, (a, b) -> ~(a & b));

        assertEquals(~(5 & 10), state.reg[2]); // ผลลัพธ์ต้องตรงกับ NAND
    }

//    @Test
//    void testExecuteLoadStoreLoad() {
//        state.reg[0] = 0; // base address
//        state.mem[0] = 42; // ค่าที่จะโหลด
//        state.mem[state.pc] = (2 << 22) | (0 << 19) | (1 << 16) | (0); // LW r1, 0(r0)
//
//        Simulator.executeLoadStore(state, true);
//
//        assertEquals(42, state.reg[1]); // ผลลัพธ์ต้องเป็น 42
//    }

    @Test
    void testExecuteLoadStoreStore() {
        // ตั้งค่าลงทะเบียนและหน่วยความจำ
        state.reg[0] = 0; // base address
        state.reg[1] = 100; // ค่าที่จะเก็บ
        state.mem[state.pc] = (3 << 22) | (0 << 19) | (1 << 16) | 0; // SW r1, 0(r0)

        Simulator.executeLoadStore(state, false);

        assertEquals(100, state.mem[0]); // mem[0] ต้องเป็น 100
    }

    @Test
    void testExecuteBranchEqual() {
        state.reg[0] = 5;
        state.reg[1] = 5;
        state.mem[0] = (4 << 22) | (0 << 19) | (1 << 16) | (2); // BEQ r0, r1, 2

        Simulator.executeBranch(state);

        assertEquals(2, state.pc); // PC ต้องเปลี่ยนไปยัง 2
    }

    @Test
    void testExecuteJALR() {
        state.reg[0] = 4; // ค่าเป้าหมาย
        state.mem[0] = (5 << 22) | (0 << 19) | (1 << 16); // JALR r0, r1

        Simulator.executeJALR(state);

        assertEquals(state.reg[0], state.pc); // PC ต้องถูกเปลี่ยนเป็นค่าของ reg[0]
    }

    @Test
    void testHalt() {
        state.mem[0] = (6 << 22);
        int initialPC = state.pc;

        Simulator.simulateMachine(state);

        assertEquals(initialPC, state.pc);
    }
}
