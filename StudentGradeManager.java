import java.util.ArrayList;
import java.util.Scanner;

/**
 * Console-based Student Grade Manager.
 * Stores students in an ArrayList, each with an ArrayList of scores.
 * Features: add/remove students, add/edit scores, per-student and class
 * statistics (average, highest, lowest), and a full summary report.
 */
public class StudentGradeManager {

    // ---------- Data model ----------
    static class Student {
        private final String id;
        private String name;
        private final ArrayList<Double> grades = new ArrayList<>();

        Student(String id, String name) {
            this.id = id;
            this.name = name;
        }

        String getId() { return id; }
        String getName() { return name; }
        ArrayList<Double> getGrades() { return grades; }

        void addGrade(double g) { grades.add(g); }

        double average() {
            if (grades.isEmpty()) return 0;
            double sum = 0;
            for (double g : grades) sum += g;
            return sum / grades.size();
        }

        double highest() {
            double max = grades.get(0);
            for (double g : grades) if (g > max) max = g;
            return max;
        }

        double lowest() {
            double min = grades.get(0);
            for (double g : grades) if (g < min) min = g;
            return min;
        }

        String letter() {
            double avg = average();
            if (avg >= 90) return "A";
            if (avg >= 80) return "B";
            if (avg >= 70) return "C";
            if (avg >= 60) return "D";
            return "F";
        }
    }

    // ---------- State ----------
    private static final ArrayList<Student> students = new ArrayList<>();
    private static final Scanner in = new Scanner(System.in);

    // ---------- Main loop ----------
    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ", 0, 8);
            System.out.println();
            switch (choice) {
                case 1: addStudent(); break;
                case 2: addGrades(); break;
                case 3: editGrade(); break;
                case 4: removeStudent(); break;
                case 5: viewStudent(); break;
                case 6: classStatistics(); break;
                case 7: summaryReport(); break;
                case 8: loadSampleData(); break;
                case 0: running = false; System.out.println("Goodbye!"); break;
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n========== STUDENT GRADE MANAGER ==========");
        System.out.println(" 1. Add student");
        System.out.println(" 2. Add grades to a student");
        System.out.println(" 3. Edit a grade");
        System.out.println(" 4. Remove student");
        System.out.println(" 5. View one student's details");
        System.out.println(" 6. Class statistics (average / highest / lowest)");
        System.out.println(" 7. Summary report of all students");
        System.out.println(" 8. Load sample data");
        System.out.println(" 0. Exit");
        System.out.println("===========================================");
    }

    // ---------- Menu actions ----------
    private static void addStudent() {
        String id = readLine("Enter student ID: ");
        if (id.isEmpty()) { System.out.println("ID cannot be empty."); return; }
        if (findById(id) != null) { System.out.println("A student with that ID already exists."); return; }
        String name = readLine("Enter student name: ");
        if (name.isEmpty()) { System.out.println("Name cannot be empty."); return; }

        Student s = new Student(id, name);
        students.add(s);
        System.out.println("Student added.");

        String more = readLine("Enter grades now? (y/n): ");
        if (more.equalsIgnoreCase("y")) enterGradesFor(s);
    }

    private static void addGrades() {
        Student s = pickStudent();
        if (s != null) enterGradesFor(s);
    }

    private static void enterGradesFor(Student s) {
        System.out.println("Enter grades (0-100) for " + s.getName() + ". Type 'done' to finish.");
        while (true) {
            String line = readLine("  Grade: ");
            if (line.equalsIgnoreCase("done")) break;
            try {
                double g = Double.parseDouble(line);
                if (g < 0 || g > 100) {
                    System.out.println("  Grade must be between 0 and 100.");
                } else {
                    s.addGrade(g);
                }
            } catch (NumberFormatException e) {
                System.out.println("  Invalid number. Try again or type 'done'.");
            }
        }
        System.out.println(s.getGrades().size() + " grade(s) recorded for " + s.getName() + ".");
    }

    private static void editGrade() {
        Student s = pickStudent();
        if (s == null) return;
        if (s.getGrades().isEmpty()) { System.out.println("This student has no grades yet."); return; }

        printGradeList(s);
        int idx = readInt("Grade number to edit: ", 1, s.getGrades().size());
        double newGrade = readDouble("New grade (0-100): ", 0, 100);
        s.getGrades().set(idx - 1, newGrade);
        System.out.println("Grade updated.");
    }

    private static void removeStudent() {
        Student s = pickStudent();
        if (s == null) return;
        students.remove(s);
        System.out.println("Removed " + s.getName() + ".");
    }

    private static void viewStudent() {
        Student s = pickStudent();
        if (s == null) return;

        System.out.println("ID:   " + s.getId());
        System.out.println("Name: " + s.getName());
        if (s.getGrades().isEmpty()) {
            System.out.println("No grades recorded.");
            return;
        }
        printGradeList(s);
        System.out.printf("Average: %.2f  |  Highest: %.2f  |  Lowest: %.2f  |  Grade: %s%n",
                s.average(), s.highest(), s.lowest(), s.letter());
    }

    private static void classStatistics() {
        ArrayList<Student> graded = studentsWithGrades();
        if (graded.isEmpty()) { System.out.println("No grades available yet."); return; }

        double total = 0;
        int count = 0;
        double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
        Student maxOwner = null, minOwner = null;

        for (Student s : graded) {
            for (double g : s.getGrades()) {
                total += g;
                count++;
                if (g > max) { max = g; maxOwner = s; }
                if (g < min) { min = g; minOwner = s; }
            }
        }

        // Best / worst student by average
        Student top = graded.get(0), bottom = graded.get(0);
        for (Student s : graded) {
            if (s.average() > top.average()) top = s;
            if (s.average() < bottom.average()) bottom = s;
        }

        System.out.println("--- Class Statistics ---");
        System.out.println("Students with grades : " + graded.size());
        System.out.println("Total scores         : " + count);
        System.out.printf("Overall average      : %.2f%n", total / count);
        System.out.printf("Highest score        : %.2f (%s)%n", max, maxOwner.getName());
        System.out.printf("Lowest score         : %.2f (%s)%n", min, minOwner.getName());
        System.out.printf("Top student (avg)    : %s (%.2f)%n", top.getName(), top.average());
        System.out.printf("Lowest student (avg) : %s (%.2f)%n", bottom.getName(), bottom.average());
    }

    private static void summaryReport() {
        if (students.isEmpty()) { System.out.println("No students to report."); return; }

        String line = "-".repeat(78);
        System.out.println(line);
        System.out.printf("%-8s %-18s %-7s %9s %9s %9s %6s%n",
                "ID", "Name", "Scores", "Average", "Highest", "Lowest", "Grade");
        System.out.println(line);

        for (Student s : students) {
            if (s.getGrades().isEmpty()) {
                System.out.printf("%-8s %-18s %-7d %9s %9s %9s %6s%n",
                        s.getId(), truncate(s.getName(), 18), 0, "-", "-", "-", "-");
            } else {
                System.out.printf("%-8s %-18s %-7d %9.2f %9.2f %9.2f %6s%n",
                        s.getId(), truncate(s.getName(), 18), s.getGrades().size(),
                        s.average(), s.highest(), s.lowest(), s.letter());
            }
        }
        System.out.println(line);

        System.out.println();
        classStatistics();
    }

    private static void loadSampleData() {
        String[][] sample = {
            {"S001", "Aarav Sharma", "88", "92", "79"},
            {"S002", "Priya Singh", "95", "98", "91"},
            {"S003", "Rohan Verma", "62", "70", "58"},
            {"S004", "Neha Gupta", "75", "81", "84"}
        };
        for (String[] row : sample) {
            if (findById(row[0]) != null) continue;
            Student s = new Student(row[0], row[1]);
            for (int i = 2; i < row.length; i++) s.addGrade(Double.parseDouble(row[i]));
            students.add(s);
        }
        System.out.println("Sample data loaded.");
    }

    // ---------- Helpers ----------
    private static Student findById(String id) {
        for (Student s : students) if (s.getId().equalsIgnoreCase(id)) return s;
        return null;
    }

    private static Student pickStudent() {
        if (students.isEmpty()) { System.out.println("No students yet. Add one first."); return null; }
        String id = readLine("Enter student ID: ");
        Student s = findById(id);
        if (s == null) System.out.println("No student found with ID '" + id + "'.");
        return s;
    }

    private static ArrayList<Student> studentsWithGrades() {
        ArrayList<Student> list = new ArrayList<>();
        for (Student s : students) if (!s.getGrades().isEmpty()) list.add(s);
        return list;
    }

    private static void printGradeList(Student s) {
        System.out.println("Grades for " + s.getName() + ":");
        for (int i = 0; i < s.getGrades().size(); i++) {
            System.out.printf("  %d. %.2f%n", i + 1, s.getGrades().get(i));
        }
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(readLine(prompt));
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("Please enter a whole number between " + min + " and " + max + ".");
        }
    }

    private static double readDouble(String prompt, double min, double max) {
        while (true) {
            try {
                double v = Double.parseDouble(readLine(prompt));
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }
}