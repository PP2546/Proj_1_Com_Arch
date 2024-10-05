//ส่วนนี้ไว้สำหรับทำการทดสอบAssembler ทุกอย่าง รวม Parse และ การแยก Type ที่เราจะแปลงเลข
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class SimulatorTest {
    private Simulator.State state;

    @BeforeEach
    void setUp() {
        state = new Simulator.State();
    }

    @Test
    public void testLoadMemoryFromFile() throws IOException {
        // สร้างไฟล์ทดสอบ
        Path tempFile = Files.createTempFile("testMemory", ".txt");
        Files.write(tempFile, "1\n2\n3\n4\n5\n".getBytes());

        // โหลดหน่วยความจำจากไฟล์
        assertTrue(Simulator.loadMemoryFromFile(state, tempFile.toString()));

        // ตรวจสอบว่าหน่วยความจำถูกโหลดอย่างถูกต้อง
        assertEquals(5, state.numMemory);
        assertEquals(1, state.mem[0]);
        assertEquals(2, state.mem[1]);
        assertEquals(3, state.mem[2]);
        assertEquals(4, state.mem[3]);
        assertEquals(5, state.mem[4]);

        // ลบไฟล์ทดสอบ
        Files.delete(tempFile);
    }
    @Test
    void testExecuteRFormatAdd() {
        // กำหนดค่าเรจิสเตอร์
        state.reg[0] = 3; // regA
        state.reg[1] = 4; // regB

        // กำหนดคำสั่ง ADD
        state.mem[0] = (0 << 22) | (0 << 19) | (1 << 16) | (2); // ADD reg[2] = reg[0] + reg[1]

        // เรียกใช้ฟังก์ชันที่ต้องการทดสอบ
        Simulator.executeRFormat(state, (a, b) -> a + b);

        // ตรวจสอบผลลัพธ์
        assertEquals(7, state.reg[2]);
    }

    @Test
    void testExecuteLoadStoreLW() {
        // กำหนดค่าหน่วยความจำ
        state.mem[10] = 42;

        // กำหนดคำสั่ง LW
        state.mem[0] = (2 << 22) | (10 << 19) | (1 << 16) | (0); // LW reg[1] = mem[reg[0] + 10]

        // กำหนดค่าเรจิสเตอร์
        state.reg[0] = 0;

        // เรียกใช้ฟังก์ชัน
        Simulator.executeLoadStore(state, true);

        // ตรวจสอบผลลัพธ์
        assertEquals(42, state.reg[1]);
    }

    @Test
    void testExecuteLoadStoreSW() {
        // กำหนดค่าเรจิสเตอร์
        state.reg[0] = 0;
        state.reg[1] = 99;

        // กำหนดคำสั่ง SW
        state.mem[0] = (3 << 22) | (0 << 19) | (1 << 16) | (10); // SW mem[reg[0] + 10] = reg[1]

        // เรียกใช้ฟังก์ชัน
        Simulator.executeLoadStore(state, false);

        // ตรวจสอบว่าหน่วยความจำถูกอัปเดต
        assertEquals(99, state.mem[10]);
    }

    @Test
    void testExecuteBranchBEQ() {
        // กำหนดค่าเรจิสเตอร์
        state.reg[0] = 5; // regA
        state.reg[1] = 5; // regB

        // กำหนดคำสั่ง BEQ
        state.mem[0] = (4 << 22) | (0 << 19) | (1 << 16) | (2); // BEQ reg[0], reg[1], offset=2

        // เรียกใช้ฟังก์ชัน
        Simulator.executeBranch(state);

        // ตรวจสอบว่า PC ถูกปรับไปยังค่า offset
        assertEquals(2, state.pc);
    }

    @Test
    void testExecuteJALR() {
        // กำหนดค่าเรจิสเตอร์
        state.reg[0] = 5; // regA
        state.reg[1] = 0; // regB

        // กำหนดคำสั่ง JALR
        state.mem[0] = (5 << 22) | (0 << 19) | (1 << 16) | (2); // JALR reg[0], reg[1]

        // เรียกใช้ฟังก์ชัน
        Simulator.executeJALR(state);

        // ตรวจสอบค่า PC และ reg[1]

        assertEquals(5, state.reg[1]); // reg[1] ควรจะเก็บค่า PC ก่อนหน้า
        assertEquals(4, state.pc); // PC ควรจะถูกปรับไปยัง reg[0]
    }

    @Test
    void testConvertNum() {
        assertEquals(-1, Simulator.convertNum(0xFFFF)); // 16-bit signed int -1
        assertEquals(0, Simulator.convertNum(0x0000)); // 16-bit signed int 0
        assertEquals(32767, Simulator.convertNum(0x7FFF)); // 16-bit signed int 32767
        assertEquals(-32768, Simulator.convertNum(0x8000)); // 16-bit signed int -32768
    }

    @Test
    void testSimulateMachine() {
        // สร้างไฟล์ทดสอบคำสั่ง
        Path tempFile = Paths.get("testInstructions.txt");
        try {
            Files.write(tempFile, "0x00000003\n0x00000004\n0x00000000\n".getBytes()); // ADD 3 + 4 = 7
            // ทดสอบการจำลองเครื่อง
            Simulator.loadMemoryFromFile(state, tempFile.toString());
            Simulator.simulateMachine(state);
            assertEquals(7, state.reg[2]); // ค่าที่คาดหวังใน reg[2]
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
