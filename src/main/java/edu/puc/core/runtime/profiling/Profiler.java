package edu.puc.core.runtime.profiling;

public class Profiler {
    static long compileTime = 0;
    static long enumerationTime = 0;
    static long executionTime = 0;
    static long numberOfMatches = 0;
    static long cleanUps = 0;

    public static void addCompileTime(long time) {
        compileTime += time;
    }

    public static void addEnumerationTime(long time) {
        enumerationTime += time;
    }

    public static void addExecutionTime(long time) {
        executionTime += time;
    }

    public static void incrementMatches() {
        numberOfMatches++;
    }

    public static void incrementCleanUps() {
        cleanUps++;
    }

    public static void print(){
//        System.out.print((double)compileTime/1000000000 + ",");
//        System.out.print((double)executionTime/1000000000 + ",");
        System.out.print((double)enumerationTime/1000000000 + ",");
        System.out.print(numberOfMatches);
//        System.err.println("Number of cleanups: " + cleanUps);
    }
}
