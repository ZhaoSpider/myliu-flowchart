package com.myliu.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

/**
 * JWT 工具类（JJWT 0.12.x）
 */
@Component
public class JwtTokenProvider {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_USERNAME = "username";

  @Value("${jwt.secret}")
  private String secret;

  /**
   * 过期时间（毫秒）
   */
  @Value("${jwt.expiration}")
  private Long expiration;

  private SecretKey getSigningKey() {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("jwt.secret is blank");
    }

    byte[] keyBytes;
    try {
      keyBytes = Decoders.BASE64.decode(secret.trim());
    } catch (Exception ignored) {
      // 兼容：直接配置普通字符串
      keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * 生成包含基础用户信息的 Token
   */
  public String generateToken(Long userId, String username) {
    Map<String, Object> claims = new HashMap<>(4);
    claims.put(CLAIM_USER_ID, userId);
    claims.put(CLAIM_USERNAME, username);

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    SecretKey key = getSigningKey();
    return Jwts.builder()
        .claims(claims)
        .subject(username)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  /**
   * 从 HTTP 头中提取裸 Token（去掉 Bearer 前缀）
   */
  public String resolveToken(String tokenOrHeader) {
    if (tokenOrHeader == null || tokenOrHeader.isBlank()) {
      return null;
    }
    String value = tokenOrHeader.trim();
    if (value.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
      return value.substring(BEARER_PREFIX.length()).trim();
    }
    return value;
  }

  /**
   * 解析 Token，返回 Claims
   */
  public Claims parseToken(String token) {
    String rawToken = resolveToken(token);
    if (rawToken == null || rawToken.isBlank()) {
      throw new IllegalArgumentException("Token is empty");
    }

    SecretKey key = getSigningKey();
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(rawToken)
        .getPayload();
  }

  /**
   * 从 Token 中获取用户名
   */
  public String getUsernameFromToken(String token) {
    return parseToken(token).getSubject();
  }

  /**
   * 从 Token 中获取用户 ID
   */
  public Long getUserIdFromToken(String token) {
    return parseToken(token).get(CLAIM_USER_ID, Long.class);
  }

  /**
   * 检查 Token 是否有效（签名合法且未过期）
   */
  public boolean validateToken(String token) {
    try {
      Claims claims = parseToken(token);
      Date expirationDate = claims.getExpiration();
      return expirationDate != null && expirationDate.after(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
