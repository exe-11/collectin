package uz.itransition.collectin.security.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.itransition.collectin.entity.enums.Role;
import uz.itransition.collectin.payload.response.APIResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uz.itransition.collectin.security.jwt.AuthConst.*;

@Component
@RequiredArgsConstructor
public class JWTokenFilter extends OncePerRequestFilter {

    private final JWTokenProvider JWTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String token = getTokenFromHeader(request);
        if (token != null) {
            try {
                final Claims claims = JWTokenProvider.parseToken(token, false);
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        claims.getSubject(),
                        null,
                        getAuthorities(claims.get(ROLE_VALUE, Integer.class))
                ));
            } catch (Exception ex) {
                setErrorResponse(ex.getMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        final String header = request.getHeader(AUTHORIZATION);
        if (header != null && header.startsWith(AUTH_TYPE))
            return header.substring(AUTH_TYPE.length());
        return null;
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        final Optional<Cookie> optionalCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(ACCESS_TOKEN) && cookie.getValue().startsWith(AUTH_TYPE)).findFirst();
        return optionalCookie.map(cookie -> cookie.getValue().substring(AUTH_TYPE.length())).orElse(null);
    }

    private Set<GrantedAuthority> getAuthorities(int roleValue) {
        final Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : Role.values()) {
            if ((roleValue & role.getFlag()) > 0)
                authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return authorities;
    }

    private ResponseEntity<?> setErrorResponse(String errorMessage) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(APIResponse.error(null, errorMessage, UNAUTHORIZED));
    }
}
