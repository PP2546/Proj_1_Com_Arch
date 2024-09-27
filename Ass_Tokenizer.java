import java.util.ArrayList;
import java.util.List;

public class Ass_Tokenizer {
    private List<String> tokens;
    private int currentIndex;

    Ass_Tokenizer(String Assembly) {
        tokens = new ArrayList<>();
        // แยกตาม \n เพื่อแยกเป็นแต่ละบรรทัดก่อน
        String[] lines = Assembly.split("\n");
        for (String line : lines) {
            String[] lineTokens = line.split("\\s+"); // แยกคำในแต่ละบรรทัด
            for (String token : lineTokens) {
                if (!token.isEmpty()) { // ลบช่องว่าง
                    tokens.add(token);
                }
            }
            tokens.add("\n"); // เพิ่มบรรทัดใหม่เป็น token ด้วยเพื่อแยกกลุ่มคำสั่ง
        }
        currentIndex = 0;
    }

    public boolean hasNext() {
        return currentIndex < tokens.size();
    }

    public String next() {
        if (hasNext()) {
            return tokens.get(currentIndex++);
        } else {
            throw new IllegalStateException("No more tokens");
        }
    }

    public void repositionToStart() {
        currentIndex = 0;
    }
}