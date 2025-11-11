package grader;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Complete exam grading system in pure Java.
 * Extracts ZIPs, grades all students using reference test cases, generates CSV and HTML reports.
 *
 * Usage: java -cp CS410-Exam.jar grader.BatchGrader <exam_folder> <test_cases_folder> <output_folder>
 */
public class BatchGrader {

    private static final String[] QUESTION_IDS = {"Q1a", "Q1b", "Q2a", "Q2b", "Q3a", "Q3b"};

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java grader.BatchGrader <exam_folder> <test_cases_folder> <output_folder>");
            System.err.println("Example: java grader.BatchGrader \"exams/CS410 Mock Exam\" \"reference_tests\" \"grading_results\"");
            System.err.println();
            System.err.println("Arguments:");
            System.err.println("  exam_folder       - Folder containing student submissions");
            System.err.println("  test_cases_folder - Folder containing reference test cases (*.test files)");
            System.err.println("  output_folder     - Folder where results will be saved");
            System.exit(1);
        }

        String examFolder = args[0];
        String testCasesFolder = args[1];
        String outputFolder = args[2];

        System.out.println("======================================================================");
        System.out.println("CS410 Exam Batch Grader (Pure Java)");
        System.out.println("======================================================================");
        System.out.println();

        try {
            // Step 1: Extract ZIP files
            int extracted = extractZipFiles(examFolder);
            System.out.println("Extracted " + extracted + " ZIP files\n");

            // Step 2: Fix nested folders
            int fixed = fixNestedFolders(examFolder);
            if (fixed > 0) {
                System.out.println("Fixed " + fixed + " nested folder structures\n");
            }

            // Step 3: Grade all students
            System.out.println("Starting batch grading...");
            System.out.println();
            List<StudentResult> results = gradeAllStudents(examFolder, testCasesFolder);

            // Step 4: Generate reports
            System.out.println("\nGenerating reports...");
            generateCsvReport(results, outputFolder);
            generateHtmlReports(results, outputFolder);
            generateIndividualPdfReports(results, outputFolder);

            System.out.println("\n======================================================================");
            System.out.println("Grading complete! All done! 🎉");
            System.out.println("======================================================================");
            System.out.println("\nResults saved to: " + outputFolder + "/");
            System.out.println("  - CSV summary: " + outputFolder + "/grading_summary.csv");
            System.out.println("  - HTML reports: " + outputFolder + "/reports/");
            System.out.println("  - Question PDFs: " + outputFolder + "/question_pdfs/");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Extract all ZIP files in the exam folder
     */
    private static int extractZipFiles(String examFolder) throws IOException {
        File examDir = new File(examFolder);
        File[] zipFiles = examDir.listFiles((dir, name) -> name.endsWith(".zip"));

        if (zipFiles == null || zipFiles.length == 0) {
            System.out.println("No ZIP files found to extract");
            return 0;
        }

        System.out.println("Found " + zipFiles.length + " ZIP files to extract...");
        int extracted = 0;

        for (File zipFile : zipFiles) {
            String studentName = zipFile.getName().replace(".zip", "");
            File extractDir = new File(examDir, studentName);

            if (extractDir.exists()) {
                System.out.println("  Skipping " + studentName + " (already extracted)");
                continue;
            }

            extractZip(zipFile, extractDir);
            System.out.println("  Extracted: " + studentName);
            extracted++;
        }

        return extracted;
    }

    /**
     * Extract a single ZIP file
     */
    private static void extractZip(File zipFile, File destDir) throws IOException {
        destDir.mkdirs();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * Fix double-nested folder structures
     */
    private static int fixNestedFolders(String examFolder) throws IOException {
        File examDir = new File(examFolder);
        File[] studentFolders = examDir.listFiles(File::isDirectory);

        if (studentFolders == null) return 0;

        int fixed = 0;
        for (File studentFolder : studentFolders) {
            if (studentFolder.getName().startsWith(".")) continue;

            File nestedFolder = new File(studentFolder, studentFolder.getName());
            if (nestedFolder.exists() && nestedFolder.isDirectory()) {
                System.out.println("  Fixing nested folder: " + studentFolder.getName());

                // Move all files from nested folder to parent
                File[] files = nestedFolder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        File dest = new File(studentFolder, file.getName());
                        if (!dest.exists()) {
                            Files.move(file.toPath(), dest.toPath());
                        }
                    }
                }

                // Remove empty nested folder
                nestedFolder.delete();
                fixed++;
            }
        }

        return fixed;
    }

    /**
     * Grade all students
     */
    private static List<StudentResult> gradeAllStudents(String examFolder, String testCasesFolder) {
        File examDir = new File(examFolder);
        File[] studentFolders = examDir.listFiles(File::isDirectory);

        if (studentFolders == null) {
            return new ArrayList<>();
        }

        List<File> folders = Arrays.stream(studentFolders)
                .filter(f -> !f.getName().startsWith("."))
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());

        System.out.println("Found " + folders.size() + " student folders");
        System.out.println();

        List<StudentResult> results = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        int completed = 0;

        for (File studentFolder : folders) {
            StudentResult result = gradeStudent(studentFolder, testCasesFolder);
            results.add(result);

            completed++;
            if (completed % 5 == 0 || completed == folders.size()) {
                long elapsed = System.currentTimeMillis() - startTime;
                double avgTime = elapsed / (double) completed;
                long remaining = (long) (avgTime * (folders.size() - completed));
                System.out.printf("Progress: %d/%d students (%.1f%%) - Est. remaining: %d seconds%n",
                        completed, folders.size(),
                        100.0 * completed / folders.size(),
                        remaining / 1000);
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.printf("\nGraded %d students in %.1f seconds%n", folders.size(), totalTime / 1000.0);

        return results;
    }

    /**
     * Grade a single student
     */
    private static StudentResult gradeStudent(File studentFolder, String testCasesFolder) {
        StudentResult result = new StudentResult();
        result.studentName = studentFolder.getName();
        result.studentFolder = studentFolder.getPath();

        for (String questionId : QUESTION_IDS) {
            ExamGrader.GradingResult gradingResult = ExamGrader.gradeQuestion(
                    studentFolder.getPath(),
                    questionId,
                    testCasesFolder
            );
            result.questions.add(gradingResult);
        }

        return result;
    }

    /**
     * Generate CSV summary report
     */
    private static void generateCsvReport(List<StudentResult> results, String outputFolder) throws IOException {
        File resultsDir = new File(outputFolder);
        resultsDir.mkdirs();

        File csvFile = new File(resultsDir, "grading_summary.csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            // Header
            writer.print("Student");
            for (String qid : QUESTION_IDS) {
                writer.print("," + qid);
            }
            writer.println(",Total,Max,Notes");

            // Each student
            for (StudentResult student : results) {
                writer.print(student.studentName);

                double total = 0;
                int totalMaxPoints = 0;
                StringBuilder notes = new StringBuilder();

                for (ExamGrader.GradingResult q : student.questions) {
                    double score = q.score != null ? q.score : 0.0;
                    writer.printf(",%.1f", score);
                    total += score;
                    totalMaxPoints += (q.maxPoints != null ? q.maxPoints : 10);

                    // Track length violations for notes column
                    if (q.regexLengthViolation != null && q.regexLengthViolation) {
                        if (notes.length() > 0) notes.append("; ");
                        notes.append(q.questionId).append(" LENGTH VIOLATION (")
                             .append(q.actualRegexLength).append("/").append(q.maxAllowedRegexLength).append(")");
                    }
                }

                writer.printf(",%.1f,%d,\"%s\"\n", total, totalMaxPoints, notes.toString());
            }
        }

        System.out.println("CSV report saved: " + csvFile.getPath());
    }

    /**
     * Generate HTML reports for all students
     */
    private static void generateHtmlReports(List<StudentResult> results, String outputFolder) throws IOException {
        File reportsDir = new File(outputFolder, "reports");
        reportsDir.mkdirs();

        for (StudentResult student : results) {
            File htmlFile = new File(reportsDir, student.studentName + ".html");
            generateStudentHtml(student, htmlFile);
        }

        System.out.println("Generated " + results.size() + " HTML reports in: " + reportsDir.getPath());
    }

    /**
     * Generate question-based PDF reports with all students' answers sorted by grade
     */
    private static void generateIndividualPdfReports(List<StudentResult> results, String outputFolder) throws IOException {
        StudentPdfExporter.exportQuestionPdfs(results, outputFolder);
    }

    /**
     * Generate HTML report for a single student (email-friendly table layout)
     */
    private static void generateStudentHtml(StudentResult student, File outputFile) throws IOException {
        // Calculate totals
        double totalScore = 0;
        int totalMaxPoints = 0;
        for (ExamGrader.GradingResult q : student.questions) {
            totalScore += (q.score != null ? q.score : 0.0);
            totalMaxPoints += (q.maxPoints != null ? q.maxPoints : 10);
        }
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>Grading Report - ").append(student.studentName).append("</title>\n");
        html.append("</head>\n<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5;\">\n");
        html.append("    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"font-family: Arial, sans-serif;\">\n");
        html.append("        <tr>\n");
        html.append("            <td>\n");

        // Header
        html.append("                <table width=\"100%\" cellpadding=\"20\" cellspacing=\"0\" border=\"0\" style=\"font-family: Arial, sans-serif; background-color: #2c3e50; color: white;\">\n");
        html.append("                    <tr>\n");
        html.append("                        <td>\n");
        html.append("                            <h1 style=\"margin: 0 0 10px 0; font-size: 24px; font-family: Arial, sans-serif;\">CS410 Exam Grading Report</h1>\n");
        html.append("                            <h2 style=\"margin: 0; font-size: 20px; font-weight: normal; font-family: Arial, sans-serif;\">").append(student.studentName).append("</h2>\n");
        html.append("                            <p style=\"margin: 10px 0 0 0; font-size: 12px; font-family: Arial, sans-serif;\">Generated on ").append(timestamp).append("</p>\n");
        html.append("                        </td>\n");
        html.append("                    </tr>\n");
        html.append("                </table>\n\n");

        // Summary
        html.append("                <table width=\"100%\" cellpadding=\"15\" cellspacing=\"0\" border=\"0\" style=\"font-family: Arial, sans-serif; background-color: #ecf0f1; margin-top: 20px;\">\n");
        html.append("                    <tr>\n");
        html.append("                        <td>\n");
        html.append("                            <h3 style=\"margin: 0 0 10px 0; font-family: Arial, sans-serif;\">Overall Score</h3>\n");
        html.append("                            <p style=\"margin: 0; font-size: 20px; font-weight: bold; font-family: Arial, sans-serif;\">");
        html.append(String.format("%.1f / %d", totalScore, totalMaxPoints));
        html.append("</p>\n");
        html.append("                        </td>\n");
        html.append("                    </tr>\n");
        html.append("                </table>\n\n");

        // Questions table
        html.append("                <table width=\"100%\" cellpadding=\"10\" cellspacing=\"0\" border=\"1\" style=\"font-family: Arial, sans-serif; margin-top: 20px; border-collapse: collapse; border: 1px solid #bdc3c7;\">\n");
        html.append("                    <tr style=\"background-color: #34495e; color: white;\">\n");
        html.append("                        <th style=\"padding: 10px; text-align: left; font-family: Arial, sans-serif;\">Question</th>\n");
        html.append("                        <th style=\"padding: 10px; text-align: center; width: 100px; font-family: Arial, sans-serif;\">Score</th>\n");
        html.append("                        <th style=\"padding: 10px; text-align: left; font-family: Arial, sans-serif;\">Details</th>\n");
        html.append("                    </tr>\n");

        // Each question
        for (ExamGrader.GradingResult q : student.questions) {
            html.append(getQuestionHtml(q));
        }

        html.append("                </table>\n\n");

        // Footer
        html.append("                <table width=\"100%\" cellpadding=\"15\" cellspacing=\"0\" border=\"0\" style=\"font-family: Arial, sans-serif; margin-top: 20px;\">\n");
        html.append("                    <tr>\n");
        html.append("                        <td style=\"text-align: center; color: #7f8c8d; font-size: 12px; font-family: Arial, sans-serif;\">\n");
        html.append("                            <p style=\"margin: 5px 0; font-family: Arial, sans-serif;\">Auto-generated grading report for CS410 Mock Exam</p>\n");
        html.append("                            <p style=\"margin: 5px 0; font-family: Arial, sans-serif;\">For questions or grade appeals, please contact your instructor</p>\n");
        html.append("                        </td>\n");
        html.append("                    </tr>\n");
        html.append("                </table>\n\n");

        html.append("            </td>\n");
        html.append("        </tr>\n");
        html.append("    </table>\n");
        html.append("</body>\n</html>\n");

        Files.write(outputFile.toPath(), html.toString().getBytes());
    }

    private static String getQuestionHtml(ExamGrader.GradingResult q) {
        double score = (q.score != null ? q.score : 0.0);
        String scoreColor;
        if (score >= 8) scoreColor = "#27ae60";  // green
        else if (score >= 4) scoreColor = "#f39c12";  // orange
        else scoreColor = "#e74c3c";  // red

        StringBuilder html = new StringBuilder();
        html.append("                    <tr>\n");
        html.append("                        <td style=\"padding: 10px; font-weight: bold; vertical-align: top;\">").append(q.questionId).append("</td>\n");
        html.append("                        <td style=\"padding: 10px; text-align: center; background-color: ").append(scoreColor).append("; color: white; font-weight: bold; vertical-align: top;\">");
        html.append(String.format("%.1f/10", score)).append("</td>\n");
        html.append("                        <td style=\"padding: 10px; vertical-align: top;\">\n");

        if (!q.success) {
            html.append("                            <table width=\"100%\" cellpadding=\"5\" cellspacing=\"0\" border=\"0\" style=\"font-family: Arial, sans-serif; background-color: #fadbd8;\">\n");
            html.append("                                <tr>\n");
            html.append("                                    <td style=\"border-left: 3px solid #e74c3c; padding-left: 10px; font-family: Arial, sans-serif;\">\n");
            html.append("                                        <strong>Error:</strong> ");
            html.append(escapeHtml(q.errorMessage != null ? q.errorMessage : "Unknown error"));
            html.append("\n                                    </td>\n");
            html.append("                                </tr>\n");
            html.append("                            </table>\n");
        } else if (q.regexLengthViolation != null && q.regexLengthViolation) {
            // Regex length violation warning
            html.append("                            <table width=\"100%\" cellpadding=\"5\" cellspacing=\"0\" border=\"0\" style=\"font-family: Arial, sans-serif; background-color: #fff3cd;\">\n");
            html.append("                                <tr>\n");
            html.append("                                    <td style=\"border-left: 3px solid #f39c12; padding-left: 10px; font-family: Arial, sans-serif;\">\n");
            html.append("                                        <strong>REGEX LENGTH VIOLATION</strong><br>\n");
            html.append("                                        Your regex exceeds the maximum allowed length.<br><br>\n");
            html.append("                                        <strong>Actual length:</strong> ").append(q.actualRegexLength).append(" characters<br>\n");
            html.append("                                        <strong>Maximum allowed:</strong> ").append(q.maxAllowedRegexLength).append(" characters<br>\n");
            html.append("                                        <strong>Exceeded by:</strong> ").append(q.actualRegexLength - q.maxAllowedRegexLength).append(" characters<br><br>\n");
            html.append("                                        <em>Note: Length is measured after removing whitespace and normalizing 'eps' to 'ε'.</em>\n");
            html.append("\n                                    </td>\n");
            html.append("                                </tr>\n");
            html.append("                            </table>\n");
        } else if (q.accuracy != null) {
            html.append("                            <table cellpadding=\"3\" cellspacing=\"0\" border=\"0\" style=\"font-family: Arial, sans-serif; font-size: 13px;\">\n");
            html.append("                                <tr>\n");
            html.append("                                    <td style=\"padding-right: 15px; font-family: Arial, sans-serif;\"><strong>Accuracy:</strong></td>\n");
            html.append("                                    <td style=\"font-family: Arial, sans-serif;\">").append(String.format("%.1f%%", q.accuracy)).append("</td>\n");
            html.append("                                    <td style=\"padding-left: 15px; padding-right: 15px; font-family: Arial, sans-serif;\"><strong>Precision:</strong></td>\n");
            html.append("                                    <td style=\"font-family: Arial, sans-serif;\">").append(String.format("%.1f%%", q.precision != null ? q.precision : 0.0)).append("</td>\n");
            html.append("                                </tr>\n");
            html.append("                                <tr>\n");
            html.append("                                    <td style=\"padding-right: 15px; font-family: Arial, sans-serif;\"><strong>Recall:</strong></td>\n");
            html.append("                                    <td style=\"font-family: Arial, sans-serif;\">").append(String.format("%.1f%%", q.recall != null ? q.recall : 0.0)).append("</td>\n");
            html.append("                                    <td style=\"padding-left: 15px; padding-right: 15px; font-family: Arial, sans-serif;\"><strong>F1 Score:</strong></td>\n");
            html.append("                                    <td style=\"font-family: Arial, sans-serif;\">").append(String.format("%.1f%%", q.f1Score != null ? q.f1Score : 0.0)).append("</td>\n");
            html.append("                                </tr>\n");
            html.append("                            </table>\n");
        }

        html.append("                        </td>\n");
        html.append("                    </tr>\n");
        return html.toString();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("\n", "<br>");
    }

    /**
     * Student result container
     */
    static class StudentResult {
        String studentName;
        String studentFolder;
        List<ExamGrader.GradingResult> questions = new ArrayList<>();
    }
}
