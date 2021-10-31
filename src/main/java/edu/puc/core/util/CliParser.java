package edu.puc.core.util;


import java.util.Scanner;

public class CliParser {
    private StringBuilder current;
    private String line;
    private String end;
    private String prompt;
    private Scanner scanner;

    public CliParser() {
        scanner = new Scanner(System.in);
        current = new StringBuilder();
        end = ";";
        prompt = "> ";
    }

    public String scan () {
        // Reset StringBuilder
        current = new StringBuilder();

        System.out.print(prompt);
        while (!(line = scanner.nextLine().trim()).endsWith(end)) {
            current.append(line).append("\n");
        }
        current.append(line);

        // Remove ending char and return the query string
        return current.deleteCharAt(current.length()-1).toString();
    }
}
