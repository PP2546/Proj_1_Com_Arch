//การแปลง Type field คำสั่ง ไปเป็นตัวเลขที่เราจะเอาไปใช้ การคำนวณ Parse ต่างๆในส่วนนี้จะเยอะเป็นพิเศษ
import java.math.BigInteger;
import java.util.*;
import static java.lang.Integer.toBinaryString;

public class Assembler {

    private final Ass_Tokenizer tokenizer;
    private List<String> tokens = new ArrayList<>();     // Tokenized instructions per line
    private int lineCounter = 0;                         // Line counter for tracking assembly lines
    private boolean isTestMode = true;
    private final Map<String, Integer> labelMap = new HashMap<>();
    private String currentMachineCode = ZERO_BITS;       // Machine code string with initial zero bits
    private static final String ZERO_BITS = "0000000";   // 7-bit zero padding for unused fields (bits 31-25)
    private final List<String> machineCodeList = new ArrayList<>();
    private String opcode;




    // Constructor to initialize the tokenizer with assembly code
    public Assembler(String assembly) {
        this.tokenizer = new Ass_Tokenizer(assembly);
    }

    // Helper to check if the token is a valid instruction
    static boolean isInstruction(String token) {
        return getOpcode(token) != null;
    }

    // Helper to check if a string is an integer
    static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }


    // Helper to check if the token is a valid label
    private boolean isValidLabel(String token) {
        return !labelMap.containsKey(token) && token.length() <= 6;
    }

    private boolean isLabel(String token){
        return labelMap.containsKey(token);
    }

    // Getter method สำหรับ labelMap
    public Map<String, Integer> getLabelMap() {
        return labelMap;
    }

    // Opcode mapping based on instructions
    public static String getOpcode(String instruction) {
        return switch (instruction) {
            case "add"      -> "000";
            case "nand"     -> "001";
            case "lw"       -> "010";
            case "sw"       -> "011";
            case "beq"      -> "100";
            case "jalr"     -> "101";
            case "halt"     -> "110";
            case "noop"     -> "111";
            case "sub"      -> "000";
            case ".fill"    -> "no-opcode";
            default         -> null;
        };
    }

    // Instruction type mapping
    public static String getInstructionType(String instruction) {
        return switch (instruction) {
            case "add", "nand", "sub"      -> "R";
            case "lw", "sw", "beq"  -> "I";
            case "jalr"             -> "J";
            case "halt", "noop"     -> "O";
            case ".fill"            -> "F";
            default                 -> null;
        };
    }

    // Number of fields based on instruction
    public static int getFieldCount(String instruction) {
        return switch (instruction) {
            case "add", "nand", "sub"       -> 3;
            case "lw", "sw", "beq", "jalr"  -> 2;
            case "halt", "noop", ".fill"    -> 0;
            default                         -> 0;
        };
    }

    private void reset() {
        tokenizer.repositionToStart();
        lineCounter = 0;
    }

    // Parses a single line of assembly into tokens
    private void parseLine() {
        tokens.clear();
        while (tokenizer.hasNext()) {
            String token = tokenizer.next();
            if (token.equals("\n")) break;
            tokens.add(token);
        }
        if (isTestMode) {
            System.out.println("------------------------------------------------------------------------------");
            System.out.println("Parsed tokens: " + tokens);
        }
    }

    // Generates machine code based on instruction types
    private List<String> generateMachineCode() {
        reset();
        parseLine();

        while (tokenizer.hasNext() || !tokens.isEmpty()) {
            int index = 0;
            if (isTestMode) System.out.println("Processing Line: " + (lineCounter + 1));

            if (tokens.isEmpty()) parseLine();

            // Check if line starts with instruction or label
            if (!isInstruction(tokens.get(index))) {
                if (!isLabel(tokens.get(index))) {
                    System.out.println("Invalid label found at the beginning of line.");
                    exitWithError();
                } else {
                    index++;
                    if (!isInstruction(tokens.get(index))) {
                        System.out.println("No instruction found after label.");
                        exitWithError();
                    }
                }
            }

            // Retrieve instruction details
            String instruction = tokens.get(index);
            String instructionType = getInstructionType(instruction);
            String opcode = getOpcode(instruction);

            if (isTestMode) {
                System.out.println("Instruction: " + instruction);
                System.out.println("Type: " + instructionType);
                System.out.println("Opcode: " + opcode);
            }

            // Prepare the machine code for the current instruction
            int fieldCount = getFieldCount(instruction);
            String[] fields = new String[3]; // Allocate space for up to 3 fields

            currentMachineCode = ZERO_BITS + opcode;

            // Parse fields and convert to binary
            for (int i = 0; i < fieldCount; ++i) {
                fields[i] = tokens.get(index + 1 + i);
                if (!isInteger(fields[i])) {
                    System.out.println("Field " + i + " is not an integer.");
                    exitWithError();
                }
                int fieldValue = Integer.parseInt(fields[i]);
                if (fieldValue < 0 || fieldValue > 7) {
                    System.out.println("Field " + i + " value out of range (0-7).");
                    exitWithError();
                }
                fields[i] = formatBinary(toBinaryString(fieldValue), 3);
            }

            // Generate machine code based on instruction type
            switch (instructionType) {
                case "R" -> generateRType(fields);
                case "I" -> generateIType(fields, index, instruction);
                case "J" -> generateJType(fields);
                case "O" -> generateOType(instruction);
                case "F" -> generateFType(index);
                default -> exitWithError();
            }

            machineCodeList.add(currentMachineCode); // Add generated machine code to list
            System.out.println("Machine Code :  "+currentMachineCode);
            lineCounter++;
            parseLine(); // Parse the next line
        }
        return machineCodeList;
    }

    // Map labels to line numbers in the assembly
    private void mapLabels() {
        while (tokenizer.hasNext() || !tokens.isEmpty()) {
            if (tokens.isEmpty()) parseLine();
            if (!isInstruction(tokens.get(0)) && isValidLabel(tokens.get(0))) {
                labelMap.put(tokens.get(0), lineCounter);
            }
            lineCounter++;
            parseLine();
        }
    }

    // Generate R-type machine code
    private void generateRType(String[] fields) {
        currentMachineCode += fields[0];  // regA
        currentMachineCode += fields[1];  // regB
        currentMachineCode += "0000000000000";  // 13-bit unused
        currentMachineCode += fields[2];  // destReg
    }

    // Generate I-type machine code
    private void generateIType(String[] fields, int index, String instruction) {
        currentMachineCode += fields[0];  // regA
        currentMachineCode += fields[1];  // regB

        int offset = resolveOffset(index + 3, instruction);
        currentMachineCode += formatBinaryWithSign(offset, 16);
    }

    // Resolve offset value for I-type instructions
    private int resolveOffset(int index, String instruction) {
        String offsetField = tokens.get(index);
        int offset;

        // Check if the offset is a valid label
        if (labelMap.containsKey(offsetField)) {
            offset = (instruction.equals("beq")) ? labelMap.get(offsetField) - lineCounter - 1 : labelMap.get(offsetField);
        }
        // Check if the offset is an integer
        else if (isInteger(offsetField)) {
            offset = Integer.parseInt(offsetField);
        }
        // Invalid offset (neither label nor integer)
        else {
            System.out.println("Invalid offset encountered: " + offsetField);
            exitWithError();
            return 0;  // Return zero to satisfy compilation; error handling already exits
        }

        // Check for valid offset range (-32768 to 32767)
        if (offset > 32767 || offset < -32768) {
            System.out.println("Offset out of range (-32768 to 32767): " + offset);
            exitWithError();
        }
        return offset;
    }

    // Generate J-type machine code
    private void generateJType(String[] fields) {
        currentMachineCode += fields[0];  // regA
        currentMachineCode += fields[1];  // regB
        currentMachineCode += "0000000000000000";  // 16-bit unused
    }

    // Generate O-type machine code (no fields)
    private void generateOType(String instruction) {
        opcode = getOpcode(instruction); // กำหนดค่า opcode ก่อน
        currentMachineCode += opcode; // เพิ่ม opcode
        currentMachineCode += "0000000000000000000000"; // 22-bit unused
    }

    // Generate F-type machine code (.fill instruction)
    private void generateFType(int index) {
        currentMachineCode = "";  // Reset for F-type
        String value = tokens.get(index + 1);
        int fillValue = isInteger(value) ? Integer.parseInt(value) : labelMap.get(value);
        currentMachineCode = formatBinaryWithSign(fillValue, 32);
    }

    public static String addZeroBits(String field, int size) {
        StringBuilder result = new StringBuilder();
        while (result.length() + field.length() < size) {
            result.append('0');
        }
        result.append(field);

        return result.toString();
    }

    // Exit program with error code
    private void exitWithError() {
        throw new IllegalArgumentException("Exiting with error code: " + 1);
    }

    // Format binary number with specific bit length
    private String formatBinary(String binary, int bitLength) {
        return "0".repeat(bitLength - binary.length()) + binary;
    }

    // Format binary number with signed bit length
    private String formatBinaryWithSign(int number, int bitLength) {
        if (number >= 0) {
            return formatBinary(toBinaryString(number), bitLength);
        } else {
            BigInteger negative = BigInteger.ONE.shiftLeft(bitLength).add(BigInteger.valueOf(number));
            return negative.toString(2);
        }
    }

    // Main method to convert assembly to machine code
    public List<String> assembleToMachineCode() {
        System.out.println("*** Label Mapping in Progress ***");
        mapLabels();                  // First pass to map labels
        if (isTestMode) System.out.println("Label Mapping: " + labelMap);
        return generateMachineCode();  // Second pass to generate machine code
    }
}
