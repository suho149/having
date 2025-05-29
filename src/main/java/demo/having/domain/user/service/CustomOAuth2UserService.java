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

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String provider = request.getClientRegistration().getRegistrationId();

        // 제공자별로 다른 속성 처리
        OAuthAttributes attributes = OAuthAttributes.of(provider,
                oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes, provider);

        return new
                CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey(),
                user
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes, String provider) {
        User user = userRepository.findByProviderAndProviderId(provider, attributes.getProviderId())
                .map(entity -> entity.update(attributes.getNickname(), attributes.getPicture()))
                .orElseGet(() -> attributes.toEntity(provider));

        return userRepository.save(user);
    }
}
