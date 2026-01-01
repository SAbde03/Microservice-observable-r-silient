package com.project.bookservice.service;

import com.project.bookservice.domain.Book;
import com.project.bookservice.repo.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repo;
    private final PricingClient pricing;

    public BookService(BookRepository repo, PricingClient pricing) {
        this.repo = repo;
        this.pricing = pricing;
    }

    public List<Book> all() {
        return repo.findAll();
    }

    public Book create(Book book) {
        repo.findByTitle(book.getTitle()).ifPresent(x -> {
            throw new IllegalArgumentException("Titre déjà existant");
        });
        return repo.save(book);
    }

    @Transactional
    public BorrowResult borrow(long id) {
        // verrou DB ici
        Book book = repo.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable"));

        book.decrementStock(); // peut lancer IllegalStateException
        double price = pricing.getPrice(id);

        return new BorrowResult(book.getId(), book.getTitle(), book.getStock(), price);
    }

    public record BorrowResult(Long id, String title, int stockLeft, double price) {}
}
