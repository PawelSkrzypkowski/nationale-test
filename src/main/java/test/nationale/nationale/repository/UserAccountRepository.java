package test.nationale.nationale.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.nationale.nationale.model.UserAccountEntity;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Long> {
    UserAccountEntity findFirstByAccIdentifier(String accIdentifier);
}
