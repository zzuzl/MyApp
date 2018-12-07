package cn.zlihj.util;

import cn.zlihj.domain.Staff;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Date;

public final class ParamUtil {
    static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    static Validator validator = factory.getValidator();
    private static final String APP = "zlihj";

    public static String md5(String s) {
        return DigestUtils.md5DigestAsHex(s.getBytes());
    }

    public static String createToken(String user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(APP);
            return JWT.create()
                    .withIssuer(user)
                    .withExpiresAt(new Date(System.currentTimeMillis() + 604800000))
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new RuntimeException("token创建失败:" + user);
        }
    }

    public static String parseToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(APP);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            if (jwt.getExpiresAt() == null || jwt.getExpiresAt().before(new Date())) {
                throw new RuntimeException("token已过期：" + jwt.getIssuer());
            }

            return jwt.getIssuer();
        } catch (JWTVerificationException exception){
            throw new RuntimeException("token解析错误:" + token);
        }
    }

    public static String createToken(String text, int minutes) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(APP);
            return JWT.create()
                    .withIssuer(text)
                    .withExpiresAt(new Date(System.currentTimeMillis() + minutes * 60000))
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new RuntimeException("token创建失败:" + text);
        }
    }

    public static <T> void checkBean(T checkable) {
        Assert.notNull(checkable, "参数为空");
        for (ConstraintViolation c : validator.validate(checkable)) {
            throw new RuntimeException(c.getPropertyPath().toString() + ":" + c.getMessage());
        }
    }

}
