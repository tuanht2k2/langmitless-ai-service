package langmitless.ai.service;

import com.kma.common.entity.Account;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ModelMapper mapper;

    public Account getCurrentAccount() {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (authenticationToken != null && authenticationToken.getPrincipal() instanceof Account) {
            return (Account) authenticationToken.getPrincipal();
        }
        return null;
    }
}
