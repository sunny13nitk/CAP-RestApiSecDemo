package restapi.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

import com.sap.cloud.security.spring.config.IdentityServicesPropertySourceFactory;
import com.sap.cloud.security.token.TokenClaims;
import com.sap.cloud.security.xsuaa.token.AuthenticationToken;

import restapi.utilities.CL_DestinationUtilities;

@Configuration
@Profile(CL_DestinationUtilities.GC_BTPProfile)
@EnableWebSecurity()
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Order(1) // needs to have higher priority than CAP security config
@PropertySource(factory = IdentityServicesPropertySourceFactory.class, ignoreResourceNotFound = true, value =
{ "" })
public class AppSecurityConfig
{
    @Autowired
    Converter<Jwt, AbstractAuthenticationToken> authConverter; // Required only when Xsuaa is used

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        // @formatter:off
        http

               .authorizeHttpRequests(authz ->
                           authz
                                .requestMatchers("/authorize/**").permitAll()
                                .requestMatchers("/logs/").hasAuthority("RESTREAD")
                                .requestMatchers("/token-user/").authenticated()
                                .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(c->c.disable())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new MyCustomHybridTokenAuthenticationConverter()))); 
        // Adjust the converter to represent your use case
        // Use MyCustomHybridTokenAuthenticationConverter when IAS and XSUAA is used
        // Use MyCustomIasTokenAuthenticationConverter when only IAS is used                                
        // @formatter:on
        return http.build();

        // return
        // http.securityMatcher(AntPathRequestMatcher.antMatcher("/authorize/")).csrf(c
        // -> c.disable()) // don't
        // // insist on
        // // csrf
        // // tokens in
        // // put, post
        // // etc.
        // .authorizeHttpRequests(r -> r.anyRequest().permitAll()).build();
    }

    /**
     * Workaround for hybrid use case until Cloud Authorization Service is globally
     * available.
     */
    class MyCustomHybridTokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken>
    {

        public AbstractAuthenticationToken convert(Jwt jwt)
        {
            if (jwt.hasClaim(TokenClaims.XSUAA.EXTERNAL_ATTRIBUTE))
            {
                return authConverter.convert(jwt);
            }
            return new AuthenticationToken(jwt, deriveAuthoritiesFromGroup(jwt));
        }

        private Collection<GrantedAuthority> deriveAuthoritiesFromGroup(Jwt jwt)
        {
            Collection<GrantedAuthority> groupAuthorities = new ArrayList<>();
            if (jwt.hasClaim(TokenClaims.GROUPS))
            {
                List<String> groups = jwt.getClaimAsStringList(TokenClaims.GROUPS);
                for (String group : groups)
                {
                    groupAuthorities.add(new SimpleGrantedAuthority(group.replace("IASAUTHZ_", "")));
                }
            }
            return groupAuthorities;
        }
    }

    /**
     * Workaround for IAS only use case until Cloud Authorization Service is
     * globally available.
     */
    static class MyCustomIasTokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken>
    {

        public AbstractAuthenticationToken convert(Jwt jwt)
        {
            final List<String> groups = jwt.getClaimAsStringList(TokenClaims.GROUPS);
            final List<GrantedAuthority> groupAuthorities = groups == null ? Collections.emptyList()
                    : groups.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            return new AuthenticationToken(jwt, groupAuthorities);
        }
    }

}
