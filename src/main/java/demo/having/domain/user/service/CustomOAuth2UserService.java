package demo.having.domain.user.service;

import demo.having.domain.user.attribute.OAuthAttributes;
import demo.having.domain.user.entity.CustomOAuth2User;
import demo.having.domain.user.entity.User;
import demo.having.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String registrationId = request.getClientRegistration().getRegistrationId(); // google, kakao 등

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes); // toEntity 호출 시 provider 인자 제거

        // user.getRole()이 이제 Role Enum 타입이므로, getKey()를 호출하여 "ROLE_USER" 형태의 문자열을 얻음
        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey(),
                user
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) { // provider 인자 제거

        Optional<User> existingUser = userRepository.findByProviderAndProviderId(attributes.getProvider().name(), attributes.getProviderId());

        User user = existingUser.map(entity -> {
            entity.update(attributes.getNickname(), attributes.getPicture());
            return entity;
        }).orElseGet(() -> attributes.toEntity());

        return userRepository.save(user);
    }
}
