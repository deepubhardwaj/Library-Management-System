package deepu.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LibraryRepository {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public int addBook(Book book) throws SQLException {
        String sql = """
                INSERT INTO books(title, author, isbn, available)
                VALUES (?, ?, ?, 1)
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());

            return ps.executeUpdate();
        }
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM books ORDER BY id";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getInt("available") == 1
                ));
            }
        }

        return books;
    }

    public List<Book> searchBooks(String keyword)
            throws SQLException {

        List<Book> books = new ArrayList<>();

        String sql = """
                SELECT * FROM books
                WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?
                ORDER BY id
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("isbn"),
                            rs.getInt("available") == 1
                    ));
                }
            }
        }

        return books;
    }

    public int updateBook(Book book) throws SQLException {
        String sql = """
                UPDATE books
                SET title = ?, author = ?, isbn = ?
                WHERE id = ?
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setInt(4, book.getId());

            return ps.executeUpdate();
        }
    }

    public int deleteBook(int id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase()
                    .contains("foreign key")) {

                throw new SQLException(
                        "This book has transaction history "
                                + "and cannot be deleted.",
                        e
                );
            }

            throw e;
        }
    }

    public int addMember(Member member) throws SQLException {
        String sql = "INSERT INTO members(name, email) VALUES (?, ?)";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());

            return ps.executeUpdate();
        }
    }

    public List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();

        String sql = "SELECT * FROM members ORDER BY id";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                members.add(new Member(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }
        }

        return members;
    }

    public boolean issueBook(int bookId, int memberId)
            throws SQLException {

        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);

            try {
                String checkMember =
                        "SELECT id FROM members WHERE id = ?";

                try (PreparedStatement ps =
                             con.prepareStatement(checkMember)) {

                    ps.setInt(1, memberId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                    }
                }

                String updateBook = """
                        UPDATE books
                        SET available = 0
                        WHERE id = ? AND available = 1
                        """;

                try (PreparedStatement ps =
                             con.prepareStatement(updateBook)) {

                    ps.setInt(1, bookId);

                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }

                String issueSql = """
                        INSERT INTO transactions
                        (book_id, member_id, issue_date, return_date)
                        VALUES (?, ?, ?, NULL)
                        """;

                try (PreparedStatement ps =
                             con.prepareStatement(issueSql)) {

                    ps.setInt(1, bookId);
                    ps.setInt(2, memberId);
                    ps.setString(
                            3, LocalDate.now().format(DATE_FORMAT)
                    );

                    ps.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }

    public boolean returnBook(int bookId) throws SQLException {
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);

            try {
                int transactionId;

                String findIssue = """
                        SELECT id FROM transactions
                        WHERE book_id = ? AND return_date IS NULL
                        ORDER BY id DESC
                        LIMIT 1
                        """;

                try (PreparedStatement ps =
                             con.prepareStatement(findIssue)) {

                    ps.setInt(1, bookId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }

                        transactionId = rs.getInt("id");
                    }
                }

                String updateTransaction = """
                        UPDATE transactions
                        SET return_date = ?
                        WHERE id = ? AND return_date IS NULL
                        """;

                try (PreparedStatement ps =
                             con.prepareStatement(updateTransaction)) {

                    ps.setString(
                            1, LocalDate.now().format(DATE_FORMAT)
                    );
                    ps.setInt(2, transactionId);

                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }

                String updateBook =
                        "UPDATE books SET available = 1 WHERE id = ?";

                try (PreparedStatement ps =
                             con.prepareStatement(updateBook)) {

                    ps.setInt(1, bookId);

                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }

    public List<String> getIssuedBooks() throws SQLException {
        List<String> records = new ArrayList<>();

        String sql = """
                SELECT m.name AS member_name,
                       m.email AS member_email,
                       b.title AS book_title,
                       b.author AS author,
                       t.issue_date AS issue_date
                FROM transactions t
                JOIN members m ON t.member_id = m.id
                JOIN books b ON t.book_id = b.id
                WHERE t.return_date IS NULL
                ORDER BY t.id DESC
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                records.add(
                        "Member: " + rs.getString("member_name")
                        + " | Email: " + rs.getString("member_email")
                        + " | Book: " + rs.getString("book_title")
                        + " | Author: " + rs.getString("author")
                        + " | Issue Date: " + formatDate(
                                rs.getString("issue_date"))
                        + " | Status: Not Returned"
                );
            }
        }

        return records;
    }

    public List<String> getIssuedBooksByMemberName(String memberName)
            throws SQLException {

        List<String> records = new ArrayList<>();

        String sql = """
                SELECT m.name AS member_name,
                       m.email AS member_email,
                       b.title AS book_title,
                       b.author AS author,
                       t.issue_date AS issue_date
                FROM transactions t
                JOIN members m ON t.member_id = m.id
                JOIN books b ON t.book_id = b.id
                WHERE LOWER(TRIM(m.name)) = LOWER(TRIM(?))
                  AND t.return_date IS NULL
                ORDER BY t.id DESC
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, memberName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(
                            "Member: " + rs.getString("member_name")
                            + " | Email: " + rs.getString("member_email")
                            + " | Book: " + rs.getString("book_title")
                            + " | Author: " + rs.getString("author")
                            + " | Issue Date: " + formatDate(
                                    rs.getString("issue_date"))
                            + " | Status: Not Returned"
                    );
                }
            }
        }

        return records;
    }

    private String formatDate(String date) {
        if (date == null || date.isBlank()) {
            return date;
        }

        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(date).format(DATE_FORMAT);
        }

        if (date.matches("\\d{2}-\\d{2}-\\d{4}")) {
            return date;
        }

        return date;
    }
}
