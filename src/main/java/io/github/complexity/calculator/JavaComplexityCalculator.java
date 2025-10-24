package io.github.complexity.calculator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates cyclomatic complexity for Java code using JavaParser
 */
public class JavaComplexityCalculator implements ComplexityCalculator {

    @Override
    public ComplexityResult calculate(String sourceCode, String fileName) throws IOException {
        JavaParser parser = new JavaParser();
        var parseResult = parser.parse(sourceCode);

        if (!parseResult.isSuccessful()) {
            throw new IOException("Failed to parse Java code: " + parseResult.getProblems());
        }

        CompilationUnit cu = parseResult.getResult().orElseThrow();
        Map<String, Integer> complexities = new HashMap<>();

        // Visit all methods and calculate their complexity
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration method, Void arg) {
                super.visit(method, arg);
                int complexity = calculateMethodComplexity(method);
                String methodName = method.getNameAsString() + method.getSignature().toString();
                complexities.put(methodName, complexity);
            }
        }, null);

        return new ComplexityResult(fileName, "Java", complexities);
    }

    private int calculateMethodComplexity(MethodDeclaration method) {
        ComplexityCounter counter = new ComplexityCounter();
        method.accept(counter, null);
        return counter.getComplexity();
    }

    @Override
    public String getLanguage() {
        return "Java";
    }

    /**
     * Visitor that counts decision points to calculate cyclomatic complexity
     * Formula: M = E - N + 2P where:
     * - E = edges in control flow graph
     * - N = nodes
     * - P = connected components
     *
     * Simplified: Start at 1, add 1 for each decision point
     */
    private static class ComplexityCounter extends VoidVisitorAdapter<Void> {
        private int complexity = 1; // Base complexity

        public int getComplexity() {
            return complexity;
        }

        // Control flow statements
        @Override
        public void visit(IfStmt n, Void arg) {
            complexity++; // if condition
            super.visit(n, arg);
        }

        @Override
        public void visit(ForStmt n, Void arg) {
            complexity++; // for loop
            super.visit(n, arg);
        }

        @Override
        public void visit(ForEachStmt n, Void arg) {
            complexity++; // for-each loop
            super.visit(n, arg);
        }

        @Override
        public void visit(WhileStmt n, Void arg) {
            complexity++; // while loop
            super.visit(n, arg);
        }

        @Override
        public void visit(DoStmt n, Void arg) {
            complexity++; // do-while loop
            super.visit(n, arg);
        }

        @Override
        public void visit(SwitchEntry n, Void arg) {
            if (!n.getLabels().isEmpty()) {
                complexity++; // each case
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(CatchClause n, Void arg) {
            complexity++; // catch block
            super.visit(n, arg);
        }

        @Override
        public void visit(ConditionalExpr n, Void arg) {
            complexity++; // ternary operator
            super.visit(n, arg);
        }

        // Logical operators
        @Override
        public void visit(BinaryExpr n, Void arg) {
            if (n.getOperator() == BinaryExpr.Operator.AND ||
                n.getOperator() == BinaryExpr.Operator.OR) {
                complexity++; // && and ||
            }
            super.visit(n, arg);
        }
    }
}
