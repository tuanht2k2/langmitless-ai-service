package langmitless.ai.JWT;

import com.kma.common.entity.Account;
import langmitless.ai.enums.EError;
import langmitless.ai.exception.CustomException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JWTUtil {
    @NonFinal
    @Value("${JWT.SIGNER_KEY}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${JWT.VALID_DURATION}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${JWT.REFRESHABLE_DURATION}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${JWT.ISSUER}")
    protected String ISSUER;


    public String generateToken(Account account) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getPhoneNumber())
                .issuer(ISSUER)
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("id", account.getId())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));

        return jwsObject.serialize();
    }


    public Boolean validateToken(String token) {
        try {
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean verified = signedJWT.verify(verifier);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return verified && expirationTime.after(new Date());
        } catch (JOSEException | ParseException e) {
            throw new CustomException(EError.UNAUTHENTICATED);
        }
    }

    public String getSubject(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new CustomException(EError.UNAUTHENTICATED);
        }
    }

}
