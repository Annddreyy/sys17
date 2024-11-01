import java.io.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        final String FILE_PATH = "Test.java";

        StringBuilder myCode = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                myCode.append(line).append("\n");
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
        }

        String codeString = myCode.toString();

        // Удаление комментариев
        codeString = codeString.replaceAll(".*//.*\n", "");

        // Удаление перехода на новую строку
        codeString = codeString.replaceAll("\n", "");

        // Удаление лишнего количества пробелов
        codeString = codeString.replaceAll("\\s{2,}", " ");

        // Удаление пробелов между знаками операций
        String[] letters = new String[] {"=", "-", "+", "/", "*"};
        for (String letter: letters) {
            String regex = " \\" + letter + " ";
            codeString = codeString.replaceAll(regex, letter);
        }

        // Удаление пробелов после знаков
        codeString = codeString.replaceAll(", ", ",");
        codeString = codeString.replaceAll("; ", ";");
        codeString = codeString.replaceAll("\\{ ", "{");

        // Удаление многострочных комментариев
        codeString = codeString.replaceAll("/\\*.*\\*/", "");

        // Все возможные классы переменных
        String[] types = new String[] {
                "String",
                "URL",
                "int",
                "InputStream",
                "StringBuilder",
                "JSONObject",
                "HttpURLConnection",
                "BufferedReader",
                "IOException",
                "Exception",
                "OutputStreamWriter",
        };

        // Добавление в отдельную строку
        StringBuilder regex = new StringBuilder();
        for (String type: types){
            regex.append("|").append(type);
        }

        // Текущее название для класса/функции/переменой
        char variableName = 65;

        String fileName = "";
        String newFile = "";

        // Поиск и замена названия класса и конструкторов
        Pattern pattern = Pattern.compile("class \\w+( )*\\{");
        Matcher matcher = pattern.matcher(codeString);
        while (matcher.find()) {
            String variable = matcher.group();
            fileName = variable.split(" ")[1].split("\\{")[0];
            newFile = createName(variableName);
            Pattern pattern1 = Pattern.compile(fileName);
            Matcher matcher1 = pattern1.matcher(codeString);
            while (matcher1.find()) {
                codeString = codeString.replace(fileName, createName(variableName));
            }
            variableName += 1;
        }

        // Поиск и замена названий методов
        pattern = Pattern.compile("(" + regex.substring(1) + "|void" + ") \\w+\\([a-zA-Z, ]*\\)");
        matcher = pattern.matcher(codeString);

        while (matcher.find()) {
            String variable = matcher.group().split(" ")[1].split("\\(")[0];
            if (!Objects.equals(variable, fileName)) {
                codeString = codeString.replaceAll(variable, createName(variableName));
                variableName += 1;
            }
        }

        // Замена для того, чтобы не было конфликта из-за спец. символов
        codeString = codeString.replaceAll("\\(", "@");
        codeString = codeString.replaceAll("\\)", "%");
        codeString = codeString.replaceAll("\\{", "`");
        codeString = codeString.replaceAll("\\+", "~");

        regex = new StringBuilder("(" + regex.substring(1) + ")");

        // Поиск и замена имен переменных
        pattern = Pattern.compile(regex + " " + "[a-zA-Z_][a-zA-Z0-9_]*(?!\\([a-zA-Z, ]*\\))");
        matcher = pattern.matcher(codeString);
        while (matcher.find()) {
            String findText = matcher.group();
            String name = createName(variableName);
            codeString = codeString.replaceAll(findText, findText.split(" ")[0] + " " + name);
            Pattern pattern1 = Pattern.compile("\\W" + findText.split(" ")[1] + "\\W");
            int count = 0;
            do {
                try {
                    Matcher matcher1 = pattern1.matcher(codeString);
                    boolean A = matcher1.find();
                    String findText1 = matcher1.group();
                    codeString = codeString.replaceAll(findText1, findText1.charAt(0) + name + findText1.charAt(findText1.length() - 1));
                    count += 1;
                }
                catch (Exception e) {break;}
            } while (count <= 10);

            variableName += 1;
        }

        codeString = codeString.replaceAll("@", "(");
        codeString = codeString.replaceAll("%", ")");
        codeString = codeString.replaceAll("`", "{");
        codeString = codeString.replaceAll("~", "+");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newFile + ".java"))) {
            writer.write(codeString);
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
        }
    }

    // Генерация нового идентификатором
    private static String createName(int nameId) {
        if (nameId < 90) return String.valueOf((char)(nameId));
        else {
            return (char) ((nameId - 90) / 26 + 65) + String.valueOf(((char)((nameId - 90) % 26 + 65)));
        }
    }
}