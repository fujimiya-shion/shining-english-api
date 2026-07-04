package vn.edu.shiningenglish.shiningenglishapi.controller.v1.city;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.repository.CityRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cities")
public class CityController extends BaseController {

    private final CityRepository cityRepository;

    public CityController(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> index() {
        var cities = cityRepository.findAllByOrderBySortOrderAscNameAsc();
        return success(cities);
    }
}
