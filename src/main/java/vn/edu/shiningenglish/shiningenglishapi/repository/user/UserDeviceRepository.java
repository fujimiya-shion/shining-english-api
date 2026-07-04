package vn.edu.shiningenglish.shiningenglishapi.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.UserDevice;

import java.time.LocalDateTime;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    @Modifying
    @Query("UPDATE UserDevice ud SET ud.loggedOutAt = :loggedOutAt, ud.lastSeenAt = :lastSeenAt WHERE ud.personalAccessTokenId = :tokenId")
    void markLoggedOutByTokenId(@Param("tokenId") Long tokenId, @Param("loggedOutAt") LocalDateTime loggedOutAt, @Param("lastSeenAt") LocalDateTime lastSeenAt);
}
