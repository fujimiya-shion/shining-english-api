package vn.edu.shiningenglish.shiningenglishapi.controller.v1.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.service.dashboard.DashboardService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController extends BaseController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> overview(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return success(dashboardService.overview(user.getId()));
    }
}
