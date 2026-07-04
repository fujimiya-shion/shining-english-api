package vn.edu.shiningenglish.shiningenglishapi.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.UserDevice;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
}
