package deepu.library;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final LibraryRepository repo =
            new LibraryRepository();

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            Database.initialize();

            System.out.println(
                    "Library database initialized successfully."
            );

            runMenu(sc);

        } catch (SQLException e) {
            System.out.println(
                    "Database error: " + e.getMessage()
            );
        }
    }

    private static void runMenu(Scanner sc) {
        while (true) {
            System.out.println(
                    "\n========== LIBRARY MANAGEMENT SYSTEM =========="
            );
            System.out.println("1. Add Book");
            System.out.println("2. View All Books");
            System.out.println("3. Search Books");
            System.out.println("4. Update Book");
            System.out.println("5. Delete Book");
            System.out.println("6. Add Member");
            System.out.println("7. View All Members");
            System.out.println("8. Issue Book");
            System.out.println("9. Return Book");
            System.out.println("10. View Issued Books");
            System.out.println("11. Search Issued Books by Member Name");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            int choice = readInt(sc);

            try {
                switch (choice) {
                    case 1 -> addBook(sc);
                    case 2 -> viewBooks(repo.getAllBooks());
                    case 3 -> searchBooks(sc);
                    case 4 -> updateBook(sc);
                    case 5 -> deleteBook(sc);
                    case 6 -> addMember(sc);
                    case 7 -> viewMembers(repo.getAllMembers());
                    case 8 -> issueBook(sc);
                    case 9 -> returnBook(sc);
                    case 10 -> viewIssuedBooks();
                    case 11 -> searchIssuedBooksByMemberName(sc);
                    case 0 -> {
                        System.out.println(
                                "Thank you for using the Library Management System!"
                        );
                        return;
                    }
                    default -> System.out.println(
                            "Invalid choice. Try again."
                    );
                }

            } catch (SQLException e) {
                System.out.println(
                        "Database operation failed: " + e.getMessage()
                );
            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Invalid input: " + e.getMessage()
                );
            }
        }
    }

    private static void addBook(Scanner sc) throws SQLException {
        String title = readText(sc, "Enter book title: ");
        String author = readText(sc, "Enter author name: ");
        String isbn = readText(sc, "Enter ISBN (unique): ");

        int result = repo.addBook(
                new Book(title, author, isbn)
        );

        System.out.println(
                result > 0
                        ? "Book added successfully."
                        : "Book was not added."
        );
    }

    private static void searchBooks(Scanner sc)
            throws SQLException {

        String keyword = readText(
                sc, "Enter title, author or ISBN to search: "
        );

        viewBooks(repo.searchBooks(keyword));
    }

    // Displays books in a properly aligned table.
    private static void viewBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        String[] headers = {
                "ID", "Title", "Author", "ISBN", "Status"
        };

        int[] widths = {5, 32, 30, 16, 12};

        StringBuilder border = new StringBuilder("+");

        for (int width : widths) {
            border.append("-".repeat(width + 2)).append("+");
        }

        String line = border.toString();

        System.out.println();
        System.out.println(line);
        printTableRow(headers, widths);
        System.out.println(line);

        for (Book book : books) {
            String[] values = {
                    String.valueOf(book.getId()),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.isAvailable() ? "Available" : "Issued"
            };

            printTableRow(values, widths);
        }

        System.out.println(line);
    }

    private static void printTableRow(
            String[] values, int[] widths) {

        System.out.print("|");

        for (int i = 0; i < widths.length; i++) {
            String value = values[i] == null ? "" : values[i];

            // Keep every column within its defined width.
            if (value.length() > widths[i]) {
                value = value.substring(0, widths[i] - 3) + "...";
            }

            System.out.printf(
                    " %-" + widths[i] + "s |", value
            );
        }

        System.out.println();
    }

    private static void updateBook(Scanner sc)
            throws SQLException {

        int id = readPositiveInt(
                sc, "Enter book ID to update: "
        );

        String title = readText(sc, "Enter new title: ");
        String author = readText(sc, "Enter new author: ");
        String isbn = readText(sc, "Enter new ISBN: ");

        int result = repo.updateBook(
                new Book(id, title, author, isbn, true)
        );

        System.out.println(
                result > 0
                        ? "Book updated successfully."
                        : "Book ID not found."
        );
    }

    private static void deleteBook(Scanner sc)
            throws SQLException {

        int id = readPositiveInt(
                sc, "Enter book ID to delete: "
        );

        int result = repo.deleteBook(id);

        System.out.println(
                result > 0
                        ? "Book deleted successfully."
                        : "Book ID not found."
        );
    }

    private static void addMember(Scanner sc)
            throws SQLException {

        String name = readText(sc, "Enter member name: ");
        String email = readText(sc, "Enter member email: ");

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }

        int result = repo.addMember(
                new Member(name, email)
        );

        System.out.println(
                result > 0
                        ? "Member added successfully."
                        : "Member was not added."
        );
    }

    private static void viewMembers(List<Member> members) {
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        String[] headers = {"ID", "Name", "Email"};
        int[] widths = {5, 30, 40};

        StringBuilder border = new StringBuilder("+");

        for (int width : widths) {
            border.append("-".repeat(width + 2)).append("+");
        }

        String line = border.toString();

        System.out.println();
        System.out.println(line);
        printTableRow(headers, widths);
        System.out.println(line);

        for (Member member : members) {
            String[] values = {
                    String.valueOf(member.getId()),
                    member.getName(),
                    member.getEmail()
            };

            printTableRow(values, widths);
        }

        System.out.println(line);
    }

    private static void issueBook(Scanner sc)
            throws SQLException {

        int bookId = readPositiveInt(
                sc, "Enter book ID: "
        );

        int memberId = readPositiveInt(
                sc, "Enter member ID: "
        );

        boolean issued = repo.issueBook(bookId, memberId);

        System.out.println(
                issued
                        ? "Book issued successfully."
                        : "Book unavailable, or book/member ID is invalid."
        );
    }

    private static void returnBook(Scanner sc)
            throws SQLException {

        int bookId = readPositiveInt(
                sc, "Enter book ID to return: "
        );

        boolean returned = repo.returnBook(bookId);

        System.out.println(
                returned
                        ? "Book returned successfully."
                        : "No active issue record found for this book."
        );
    }

    private static void viewIssuedBooks()
            throws SQLException {

        List<String> records = repo.getIssuedBooks();

        printIssuedRecords(
                records, "CURRENTLY ISSUED BOOKS"
        );
    }

    private static void searchIssuedBooksByMemberName(
            Scanner sc) throws SQLException {

        String memberName = readText(
                sc, "Enter member name: "
        );

        List<String> records =
                repo.getIssuedBooksByMemberName(memberName);

        if (records.isEmpty()) {
            System.out.println(
                    "No currently issued books found for member: "
                            + memberName
            );
            return;
        }

        System.out.println(
                "\n========== BOOKS ISSUED BY "
                        + memberName + " =========="
        );

        System.out.println(
                "Total currently issued books: " + records.size()
        );

        printIssuedRecords(records, "MEMBER ISSUE DETAILS");
    }

    private static void printIssuedRecords(
            List<String> records, String heading) {

        if (records.isEmpty()) {
            System.out.println("No currently issued books found.");
            return;
        }

        System.out.println("\n========== " + heading + " ==========");

        for (String record : records) {
            System.out.println("-----------------------------------------");

            String[] details = record.split(" \\| ");

            for (String detail : details) {
                System.out.println(detail);
            }

            System.out.println("-----------------------------------------");
        }
    }

    private static String readText(
            Scanner sc, String prompt) {

        System.out.print(prompt);
        String value = sc.nextLine().trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Input cannot be empty."
            );
        }

        return value;
    }

    private static int readInt(Scanner sc) {
        String value = sc.nextLine().trim();

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int readPositiveInt(
            Scanner sc, String prompt) {

        while (true) {
            System.out.print(prompt);

            int value = readInt(sc);

            if (value > 0) {
                return value;
            }

            System.out.println(
                    "Please enter a positive whole number."
            );
        }
    }
}
