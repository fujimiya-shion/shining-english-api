package vn.edu.shiningenglish.shiningenglishapi.repository.star;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.StarTransaction;

public interface StarTransactionRepository extends JpaRepository<StarTransaction, Long> {
}
