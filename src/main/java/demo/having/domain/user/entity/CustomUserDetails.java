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

    public User getUser() { // Helper to get your User entity
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assuming your User entity has a 'role' field like "ROLE_USER", "ROLE_ADMIN"
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Using email as the username for Spring Security
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or implement your logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Or implement your logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or implement your logic
    }

    @Override
    public boolean isEnabled() {
        return user.getDeletedAt() == null; // Account is enabled if not soft-deleted
    }
}
