package io.github.complexity.calculator;

import java.util.Map;

/**
 * Result of cyclomatic complexity calculation
 */
public class ComplexityResult {
    /**
     * Complexity threshold above which a warning indicator is shown
     */
    public static final int COMPLEXITY_WARNING_THRESHOLD = 10;

    private final String fileName;
    private final String language;
    private final Map<String, Integer> functionComplexities;
    private final int totalComplexity;
    private final int maxComplexity;
    private final String mostComplexFunction;

    public ComplexityResult(String fileName, String language,
                          Map<String, Integer> functionComplexities) {
        this.fileName = fileName;
        this.language = language;
        this.functionComplexities = functionComplexities;
        this.totalComplexity = functionComplexities.values().stream()
            .mapToInt(Integer::intValue).sum();

        var maxEntry = functionComplexities.entrySet().stream()
            .max(Map.Entry.comparingByValue());

        this.maxComplexity = maxEntry.map(Map.Entry::getValue).orElse(0);
        this.mostComplexFunction = maxEntry.map(Map.Entry::getKey).orElse("N/A");
    }

    public String getFileName() {
        return fileName;
    }

    public String getLanguage() {
        return language;
    }

    public Map<String, Integer> getFunctionComplexities() {
        return functionComplexities;
    }

    public int getTotalComplexity() {
        return totalComplexity;
    }

    public int getMaxComplexity() {
        return maxComplexity;
    }

    public String getMostComplexFunction() {
        return mostComplexFunction;
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("File: %s (%s)%n", fileName, language));
        sb.append(String.format("Total Functions: %d%n", functionComplexities.size()));
        sb.append(String.format("Total Complexity: %d%n", totalComplexity));
        sb.append(String.format("Max Complexity: %d in %s%n", maxComplexity, mostComplexFunction));
        sb.append(String.format("%nFunction Complexities:%n"));

        functionComplexities.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> {
                String indicator = e.getValue() > COMPLEXITY_WARNING_THRESHOLD ? " ⚠️" : "";
                sb.append(String.format("  %s: %d%s%n", e.getKey(), e.getValue(), indicator));
            });

        return sb.toString();
    }
}
