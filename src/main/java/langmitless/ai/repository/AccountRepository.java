package langmitless.ai.repository;

import com.kma.common.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
    Account findByPhoneNumber(String phoneNumber);
}
