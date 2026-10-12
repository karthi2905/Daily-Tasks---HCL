package com.technova.library.model;

import java.util.Objects;

/**
 * Represents a library book in the catalogue.
 * Demonstrates validation and availability state transitions.
 */
public class Book {

    private final String bookId;
    private final String title;
    private final String author;
    private boolean available;

    /**
     * Constructs a new Book with initial availability set to true.
     *
     * @param bookId unique book identifier (non-blank)
     * @param title  title of the book (non-blank)
     * @param author author of the book (non-blank)
     * @throws IllegalArgumentException if any field is null or blank
     */
    public Book(String bookId, String title, String author) {
        this(bookId, title, author, true);
    }

    /**
     * Constructs a Book with an explicit availability status.
     */
    public Book(String bookId, String title, String author, boolean available) {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be null or blank");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be null or blank");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Book author cannot be null or blank");
        }
        this.bookId = bookId.trim();
        this.title = title.trim();
        this.author = author.trim();
        this.available = available;
    }

    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Marks the book as borrowed.
     *
     * @throws IllegalStateException if the book is already unavailable
     */
    public void borrowBook() {
        if (!available) {
            throw new IllegalStateException(String.format("Book '%s' (%s) is already unavailable.", title, bookId));
        }
        this.available = false;
    }

    /**
     * Returns the book to the catalogue, restoring availability.
     */
    public void returnBook() {
        this.available = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return Objects.equals(bookId, book.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    @Override
    public String toString() {
        return String.format("[%s] \"%s\" by %s (%s)",
                bookId, title, author, available ? "AVAILABLE" : "CHECKED OUT");
    }
}
