package vn.edu.shiningenglish.shiningenglishapi.service.star;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.shiningenglish.shiningenglishapi.enums.StarTransactionType;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Star;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.StarTransaction;
import vn.edu.shiningenglish.shiningenglishapi.repository.star.*;

@Service
public class StarService {

    private final StarRepository starRepository;
    private final StarTransactionRepository starTransactionRepository;

    public StarService(StarRepository starRepository, StarTransactionRepository starTransactionRepository) {
        this.starRepository = starRepository;
        this.starTransactionRepository = starTransactionRepository;
    }

    @Transactional
    public boolean addStarByUserId(int amount, Long userId, String message, StarTransactionType type) {
        var record = starRepository.findForUpdateByUserId(userId);
        if (record.isEmpty()) {
            var star = new Star();
            star.setUserId(userId);
            star.setAmount(amount);
            starRepository.save(star);
        } else {
            var existing = record.get();
            existing.setAmount(existing.getAmount() + amount);
            starRepository.save(existing);
        }

        var tx = new StarTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType(type != null ? type : (amount >= 0 ? StarTransactionType.increase : StarTransactionType.decrease));
        tx.setDescription(message);
        starTransactionRepository.save(tx);

        return true;
    }

    @Transactional
    public boolean spendStarByUserId(int amount, Long userId, String message, StarTransactionType type) {
        if (amount <= 0) return true;

        var record = starRepository.findForUpdateByUserId(userId);
        if (record.isEmpty() || record.get().getAmount() < amount) {
            return false;
        }

        var star = record.get();
        star.setAmount(star.getAmount() - amount);
        starRepository.save(star);

        var tx = new StarTransaction();
        tx.setUserId(userId);
        tx.setAmount(-amount);
        tx.setType(type != null ? type : StarTransactionType.decrease);
        tx.setDescription(message);
        starTransactionRepository.save(tx);

        return true;
    }

    public int getBalance(Long userId) {
        return starRepository.findByUserId(userId)
            .map(Star::getAmount)
            .orElse(0);
    }
}
