import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class main {

    private static final String INPUT_DIR = "Input/";
    private static final String OUTPUT_DIR = "Output/";
    private static final String FILE_EXTENSION = ".s";
    private static final String[] INPUT_FILES = {"TEST", "COMBINATION", "MULTIPLIER", "SUM"};
    private static final String[] OUTPUT_EXTENSIONS = {".bin", ".txt"};

    // Displays the list of available input files
    private static void displayFileOptions() {
        System.out.println("Select a file to process: " + Arrays.toString(INPUT_FILES));
    }

    // Gets the user's selection for which file to process
    private static int getUserSelection() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the file number (1-" + INPUT_FILES.length + "): ");
        return Integer.parseInt(scanner.nextLine()) - 1;
    }

    // Prompts the user with a message
    private static void promptUser(String message) {
        System.out.println(message);
        new Scanner(System.in).nextLine();
    }

    // Converts the assembly file to machine code
    private static void convertToMachineCode(String fileName) {
        String assemblyCode = readFromFile(INPUT_DIR + fileName + FILE_EXTENSION);
        Assembler assembler = new Assembler(assemblyCode);

        List<String> binaryCodes = assembler.assembleToMachineCode();
        List<String> decimalCodes = Ass_Conver_Binary.binaryToDecimal(binaryCodes);

        // Save the machine code to a file
        writeToFile(OUTPUT_DIR + fileName + OUTPUT_EXTENSIONS[1], decimalCodes);
    }

    // Displays the assembly code from the input file
    private static void displayAssemblyFile(String fileName) {
        String assemblyPath = INPUT_DIR + fileName + FILE_EXTENSION;
        System.out.println("\nDisplaying assembly code from: " + assemblyPath);
        System.out.println(readFromFile(assemblyPath));
    }

    // Displays the output file in the console
    private static String displayOutputFile(String fileName, String extension) {
        String outputPath = OUTPUT_DIR + fileName + extension;
        System.out.println("\nDisplaying output from: " + outputPath);

        if (Objects.equals(extension, OUTPUT_EXTENSIONS[1])) {
            System.out.println("Decimal code output:");
        } else if (Objects.equals(extension, OUTPUT_EXTENSIONS[0])) {
            System.out.println("Binary code output:");
        }

        System.out.println(readFromFile(outputPath));
        return outputPath;
    }

    // Writes a list of strings to a file at the specified path
    public static void writeToFile(String path, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            System.out.println("Writing output to: " + path);
            for (String line : lines) {
                writer.write(line);
                writer.newLine(); // Writes a newline character
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    // Reads file content as a single string from the specified path
    public static String readFromFile(String path) {
        StringBuilder fileContent = new StringBuilder();
        try (Scanner scanner = new Scanner(new File(path))) {
            while (scanner.hasNextLine()) {
                fileContent.append(scanner.nextLine()).append(System.lineSeparator());
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }
        return fileContent.toString();
    }

    public static void main(String[] args) {
        displayFileOptions();
        int selectedFileIndex = getUserSelection();

        String selectedFile = INPUT_FILES[selectedFileIndex];
        displayAssemblyFile(selectedFile);

        promptUser("Press Enter to convert assembly to machine code...");

        convertToMachineCode(selectedFile);
        String outfile = displayOutputFile(selectedFile, OUTPUT_EXTENSIONS[1]);
        Simulator.main(new String[] { outfile });
    }
}

