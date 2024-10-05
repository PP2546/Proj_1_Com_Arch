//ส่วนนี้จะเป็นการเอาไฟล์ที่ได้จาก การผ่านโปรแกรม Assembler ซึ่งได้ machine code มาทำเป็น instruction เพื่อให้ได้ Result
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Simulator {
    private static final int NUMMEMORY = 65536; // จำนวนหน่วยความจำสูงสุด
    private static final int NUMREGS = 8; // จำนวนเรจิสเตอร์ของเครื่อง
    private static final int MAXLINELENGTH = 5000; // จำนวนคำสั่งสูงสุดสำหรับการทดสอบ

    // โครงสร้างที่เก็บสถานะของเครื่อง
    public static class State {
        int pc = 0; // Program Counter (ชี้ไปยังคำสั่งถัดไป)
        int[] mem = new int[NUMMEMORY]; // หน่วยความจำ
        int[] reg = new int[NUMREGS]; // เรจิสเตอร์
        int numMemory = 0; // จำนวนหน่วยความจำที่ถูกใช้
    }

    // ฟังก์ชันสำหรับแสดงสถานะของเครื่อง
    public static void printState(State state) {
        System.out.println("\n@@@\nState:");
        System.out.println("\tPC: " + state.pc);
        System.out.println("\tMemory:");
        for (int i = 0; i < state.numMemory; i++) {
            System.out.println("\t\tmem[" + i + "] = " + state.mem[i]);
        }
        System.out.println("\tRegisters:");
        for (int i = 0; i < NUMREGS; i++) {
            System.out.println("\t\treg[" + i + "] = " + state.reg[i]);
        }
        System.out.println("End of State\n");
    }

    // ฟังก์ชันหลักที่ควบคุมการทำงานของเครื่องจำลอง
    public static void main(String[] args) {
        String fileName;

        // ตรวจสอบว่าได้รับ argument หรือไม่
        if (args.length > 0) {
            // ใช้พาธไฟล์ที่ส่งมาจาก main ตัวแรก
            fileName = args[0];
        } else {
            // ถ้าไม่ได้รับ argument ให้เลือกไฟล์ใหม่จาก Output directory
            fileName = selectFile("Output/");
        }

        if (fileName == null) return;

        // โหลดไฟล์และตั้งค่าหน่วยความจำ
        State state = new State();
        if (!loadMemoryFromFile(state, fileName)) return;

        // เริ่มจำลองการทำงานของเครื่อง
        simulateMachine(state);
    }

    // ฟังก์ชันเลือกไฟล์
    private static String selectFile(String folderPath) {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null || listOfFiles.length == 0) {
            System.err.println("Error: No files found in Output folder");
            return null;
        }

        System.out.println("Select a file to read:");
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println(i + ": " + listOfFiles[i].getName());
            }
        }
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file number: ");
        int fileIndex = scanner.nextInt();

        if (fileIndex < 0 || fileIndex >= listOfFiles.length || !listOfFiles[fileIndex].isFile()) {
            System.err.println("Error: Invalid file selection");
            return null;
        }

        return folderPath + listOfFiles[fileIndex].getName();
    }

    // ฟังก์ชันโหลดหน่วยความจำจากไฟล์
    protected static boolean loadMemoryFromFile(State state, String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                state.mem[state.numMemory] = Integer.parseInt(line);
                state.numMemory++;
            }
            System.out.println("Loaded " + state.numMemory + " words into memory.");
            return true;
        } catch (IOException e) {
            System.err.println("Error: Can't open file " + fileName);
            e.printStackTrace();
            return false;
        }
    }

    // ฟังก์ชันจำลองการทำงานของเครื่อง
    protected static void simulateMachine(State state) {
        int totalInstructions = 0;

        while (true) {
            printState(state);
            int opcode = state.mem[state.pc] >> 22;

            switch (opcode) {
                case 0: // ADD
                    executeRFormat(state, (a, b) -> a + b);
                    break;
                case 1: // NAND
                    executeRFormat(state, (a, b) -> ~(a & b));
                    break;
                case 2: // LW
                    executeLoadStore(state, true);
                    break;
                case 3: // SW
                    executeLoadStore(state, false);
                    break;
                case 4: // BEQ
                    executeBranch(state);
                    break;
                case 5: // JALR
                    executeJALR(state);
                    break;
                case 6: // HALT
                    System.out.println("Machine halted.");
                    System.out.println("Total of " + totalInstructions + " instructions executed.");
                    return;
                case 7: // NOOP
                    break;
                default:
                    System.err.println("Unknown opcode: " + opcode);
                    return;
            }

            state.pc++;
            totalInstructions++;

            if (totalInstructions > MAXLINELENGTH) {
                System.out.println("Max instruction length reached.");
                break;
            }
        }

        System.out.println("Final state of the machine:");
        printState(state);
    }

    // ฟังก์ชันสำหรับคำสั่ง R-Format (ADD, NAND)
    protected static void executeRFormat(State state, ArithmeticOperation operation) {
        int[] args = decodeRFormat(state.mem[state.pc]);
        state.reg[args[2]] = state.reg[args[0]] + state.reg[args[1]];
    }

    // ฟังก์ชันสำหรับคำสั่ง Load/Store (LW, SW)
    protected static void executeLoadStore(State state, boolean isLoad) {
        int[] args = decodeIFormat(state.mem[state.pc]);
        int offset = args[2] + state.reg[args[0]];

        if (isLoad) {
            state.reg[args[1]] = state.mem[offset];
        } else {
            state.mem[offset] = state.reg[args[1]];
        }
    }

    // ฟังก์ชันสำหรับคำสั่ง BEQ
    protected static void executeBranch(State state) {
        int[] args = decodeIFormat(state.mem[state.pc]);
        if (state.reg[args[0]] == state.reg[args[1]]) {
            state.pc += args[2];
        }
    }

    // ฟังก์ชันสำหรับคำสั่ง JALR
    protected static void executeJALR(State state) {
        int[] args = decodeRFormat(state.mem[state.pc]);
        state.reg[args[1]] = state.pc + 1;
        state.pc = state.reg[args[0]];
        //state.pc = state.reg[args[0]] - 1;
    }

    // ฟังก์ชันถอดรหัส R-Format
    private static int[] decodeRFormat(int instruction) {
        return new int[]{
                (instruction >> 19) & 7, // regA
                (instruction >> 16) & 7, // regB
                instruction & 7           // destReg
        };
    }

    // ฟังก์ชันถอดรหัส I-Format
    private static int[] decodeIFormat(int instruction) {
        return new int[]{
                (instruction >> 19) & 7, // regA
                (instruction >> 16) & 7, // regB
                convertNum(instruction & 0xFFFF) // offset
        };
    }

    // ฟังก์ชันแปลงเลข 16-bit ให้เป็น signed integer
    protected static int convertNum(int num) {
        if ((num & (1 << 15)) != 0) {
            return num - (1 << 16);
        }
        return num;
    }

    // อินเตอร์เฟซสำหรับคำนวณแบบกำหนดเอง
    @FunctionalInterface
    interface ArithmeticOperation {
        int apply(int a, int b);
    }
}
