package demo.having.domain.user.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // user.getRole()이 Role Enum 타입이므로 getKey()를 호출
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getKey()));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // 일반 로그인 사용 시
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getDeletedAt() == null;
    }
}

// CustomOAuth2User는 UserDetails도 구현하고 있으므로, CustomUserDetails는 일반 로그인(ID/PW) 시에만 사용되거나,
// 혹은 CustomOAuth2User가 CustomUserDetails를 상속받아 UserDetails 기능을 통합할 수도 있습니다.
// 현재 분리된 상태로 유지해도 괜찮습니다.