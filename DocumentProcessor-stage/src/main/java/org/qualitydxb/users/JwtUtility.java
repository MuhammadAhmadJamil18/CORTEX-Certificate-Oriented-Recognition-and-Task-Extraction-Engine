package org.qualitydxb.users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.qualitydxb.dal.Models.User;
import org.qualitydxb.infrastructure.SystemProperties;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtility {

    private final String SECRET_KEY = SystemProperties.getSecretKey();

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public Long extractClientId(String token) {
        return extractClaim(token, claims -> claims.get("clientId", Long.class));
    }

    public Date extractLoginTime(String token) {
        return extractClaim(token, claims -> claims.get("loginTime", Date.class));
    }

    public Integer extractUserRole(String token) {
        return extractClaim(token, claims -> claims.get("userRole", Integer.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // Ensure this is correctly set up
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
       try {
            Date expiration = extractExpiration(token);
            return expiration != null && expiration.before(new Date());
       } catch (ExpiredJwtException e) {
            return true;
       }
    }


    public String generateToken(User user, boolean isResetPassword) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.userId);
        claims.put("clientId", user.clientId);
        claims.put("loginTime", new Date());
        claims.put("userRole", user.userRole);
        claims.put("email",user.userEmail);
        return createToken(claims, user.userEmail, isResetPassword);
    }

    private String createToken(Map<String, Object> claims, String subject, boolean isResetPassword) {
        if(isResetPassword) {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5))
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();
        } else {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();
        }
    }

}
