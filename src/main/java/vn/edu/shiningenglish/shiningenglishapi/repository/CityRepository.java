package vn.edu.shiningenglish.shiningenglishapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.City;
import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findAllByOrderBySortOrderAscNameAsc();
}
