package edu.puc.core.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtils {
    private static final Set<Character> validQuotes = Stream.of('\'', '"', '`').collect(Collectors.toSet());

    public static String removeQuotes(String string) {

        char quote = string.charAt(0);
        if (!validQuotes.contains(quote)) {
            throw new Error("Badly formatted quoted string: <" + quote + "> is not a valid quote character.");
        }
        if (string.charAt(string.length() - 1) != quote) {
            throw new Error("Badly formatted quoted string: quote characters not present at both ends.");
        }
        return string.substring(1, string.length() - 1);
    }

    public static boolean hasQuotes(String string) {
        char quote = string.charAt(0);
        return validQuotes.contains(quote) && string.charAt(string.length() - 1) == quote;
    }

    public static String tryRemoveQuotes(String string) {
        if (hasQuotes(string)) {
            return removeQuotes(string);
        }
        return string;
    }

    public static String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedWriter getWriter(String path) throws IOException {
        return getWriter(path, false);
    }


    public static BufferedWriter getWriter(String path, boolean append) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(file, append);
        return new BufferedWriter(fw);
    }

    public static BufferedReader getReader(String filePath) throws FileNotFoundException {
        FileReader fr = new FileReader(filePath);
        return new BufferedReader(fr);
    }
}
