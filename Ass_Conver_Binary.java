import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.List;
//
////การแปลงผลส่วนใหญ่จะอยู่ที่นี่ ไม่ว่าจะการแปลงจาก ฐานสองไปสิบ หรือ สิบไปสอง หรือ การทำติดลบ
public class Ass_Conver_Binary {

    public static String addZeroBits(String field, int size) {
        StringBuilder result = new StringBuilder();
        while (result.length() + field.length() < size) {
            result.append('0');
        }
        result.append(field);

        return result.toString();
    }

    public static List<String> binaryToDecimal(List<String> binaryList) {
        List<String> decimalList = new ArrayList<>();

        for (String binaryString : binaryList) {
            String decimal;

            if (binaryString.charAt(0) == '1') {
                StringBuilder invertedString = new StringBuilder();
                for (char bit : binaryString.toCharArray()) {
                    invertedString.append((bit == '0') ? '1' : '0');
                }

                BigInteger absoluteValue = new BigInteger(invertedString.toString(), 2).add(BigInteger.ONE);
                decimal = "-" + absoluteValue.toString();
            } else {
                decimal = new BigInteger(binaryString, 2).toString();
            }

            decimalList.add(decimal);
        }

        return decimalList;
    }

    public static List<String> decimalToBinary(List<String> decimalList) {
        List<String> binaryStrings = new ArrayList<>();

        for (String decimal : decimalList) {
            String binary;

            if (decimal.startsWith("-")) {
                String absoluteValue = decimal.substring(1);    // Remove the minus sign
                BigInteger absoluteBigInt = new BigInteger(absoluteValue);
                String binaryString = absoluteBigInt.toString(2);

                // Use twosComplement to get the binary representation of the absolute value
                binary = twosCompliment(addZeroBits(binaryString, 32));
            } else {
                // Handle positive numbers
                BigInteger positiveBigInt = new BigInteger(decimal);
                binary = positiveBigInt.toString(2);

                // Add leading zeros using addZeroBits function
                binary = addZeroBits(binary, 32);
            }

            binaryStrings.add(binary);
        }

        return binaryStrings;
    }

    public static String twosCompliment(String binary) {
        // Invert the bits
        StringBuilder inverted = new StringBuilder();
        for (char bit : binary.toCharArray()) {
            inverted.append((bit == '0') ? '1' : '0');
        }

        // Add 1 to the inverted value
        int carry = 1;
        StringBuilder result = new StringBuilder();
        for (int i = inverted.length() - 1; i >= 0; i--) {
            int bit = Character.getNumericValue(inverted.charAt(i)) + carry;
            if (bit > 1) {
                carry = 1;
                bit = 0;
            } else {
                carry = 0;
            }
            result.insert(0, bit);
        }
        return addZeroBits(result.toString(),binary.length());
    }



//
//    public static List<String> binaryToDecimal(List<String> binaryList) {
//        List<String> decimalList = new ArrayList<>();
//
//        for (String binaryString : binaryList) {
//            BigInteger value = new BigInteger(binaryString, 2);
//
//            // เช็คว่าเป็นจำนวนลบหรือไม่
//            if (binaryString.charAt(0) == '1') {
//                // สำหรับจำนวนลบ ใช้ Two's Complement
//                value = value.subtract(BigInteger.ONE.shiftLeft(binaryString.length()));
//            }
//
//            decimalList.add(value.toString());
//        }
//
//        return decimalList;
//    }
//
//    public static List<String> decimalToBinary(List<String> decimalList) {
//        List<String> binaryStrings = new ArrayList<>();
//
//        for (String decimal : decimalList) {
//            BigInteger bigIntValue = new BigInteger(decimal);
//            String binary;
//
//            if (bigIntValue.signum() < 0) {
//                // สำหรับจำนวนลบ ใช้ Two's Complement
//                binary = bigIntValue.add(BigInteger.ONE.shiftLeft(32)).toString(2);
//            } else {
//                // สำหรับจำนวนบวก
//                binary = bigIntValue.toString(2);
//            }
//
//            binaryStrings.add(addZeroBits(binary, 32)); // แทรกศูนย์ข้างหน้าให้ครบ 32 บิต
//        }
//
//        return binaryStrings;
//    }
//
//    private static String addZeroBits(String binary, int totalBits) {
//        return "0".repeat(totalBits - binary.length()) + binary;
//    }
//
}


