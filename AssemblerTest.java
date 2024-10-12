import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AssemblerTest {

    private Assembler assembler;

    @BeforeEach
    public void setUp() {
        // Set up initial conditions for each test case
        String assemblyCode = ""; // จะใช้สำหรับการทดสอบแต่ละกรณี
        assembler = new Assembler(assemblyCode);
    }

    @Test
    public void testIsInstruction() {
        assertTrue(Assembler.isInstruction("add"));
        assertFalse(Assembler.isInstruction("invalid"));
    }

    @Test
    public void testIsInteger() {
        assertTrue(Assembler.isInteger("123"));
        assertFalse(Assembler.isInteger("abc"));
    }

    @Test
    public void testGetOpcode() {
        assertEquals("000", Assembler.getOpcode("add"));
        assertNull(Assembler.getOpcode("invalid"));
    }

    @Test
    public void testGetInstructionType() {
        assertEquals("R", Assembler.getInstructionType("add"));
        assertEquals("I", Assembler.getInstructionType("lw"));
        assertNull(Assembler.getInstructionType("invalid"));
    }

    @Test
    public void testGetFieldCount() {
        assertEquals(3, Assembler.getFieldCount("add"));
        assertEquals(2, Assembler.getFieldCount("lw"));
        assertEquals(0, Assembler.getFieldCount("halt"));
    }

    @Test
    public void testGenerateMachineCode() {
        String assemblyCode = "add 1 2 3\n";
        assembler = new Assembler(assemblyCode);
        List<String> machineCode = assembler.assembleToMachineCode();
        assertEquals(1, machineCode.size());
    }

    @Test
    public void testGenerateOTypeNoop() {
        String assemblyCode = "noop\n";
        assembler = new Assembler(assemblyCode);
        List<String> machineCode = assembler.assembleToMachineCode();

        // คาดหวังว่า machine code สำหรับ noop จะเป็น "00000001111110000000000000000000000"
        assertEquals("00000001111110000000000000000000000", machineCode.get(0));
    }

    @Test
    public void testGenerateOTypeHalt() {
        String assemblyCode = "halt\n";
        assembler = new Assembler(assemblyCode);
        List<String> machineCode = assembler.assembleToMachineCode();

        // คาดหวังว่า machine code สำหรับ halt จะเป็น "00000001101100000000000000000000000"
        assertEquals("00000001101100000000000000000000000", machineCode.get(0));
    }

    @Test
    public void testFillInstruction() {
        String assemblyCode = ".fill 12345\n";
        assembler = new Assembler(assemblyCode);
        List<String> machineCode = assembler.assembleToMachineCode();

        // แปลง 12345 เป็น 32-bit binary string
        String expectedMachineCode = String.format("%32s", Integer.toBinaryString(12345)).replace(' ', '0');

        assertEquals(expectedMachineCode, machineCode.get(0)); // ค่าที่คาดหวัง
    }

    @Test
    public void testErrorHandling() {
        String assemblyCode = "add 1 a 3\n"; // 'a' ไม่ใช่ Integer
        assembler = new Assembler(assemblyCode);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assembler.assembleToMachineCode();
        });
        assertEquals("Exiting with error code: 1", exception.getMessage());
    }
}
