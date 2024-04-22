package restapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.sap.cloud.security.spring.config.IdentityServicesPropertySourceFactory;

@Configuration
@EnableWebSecurity()
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Order(1) // needs to have higher priority than CAP security config
@PropertySource(factory = IdentityServicesPropertySourceFactory.class, ignoreResourceNotFound = true, value =
{ "" })
public class AppSecurityConfig
{
    // @Autowired
    // Converter<Jwt, AbstractAuthenticationToken> authConverter; // Required only
    // when Xsuaa is used

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        // // @formatter:off
        // http
        //        .authorizeHttpRequests(authz ->
        //                    authz
        //                         .requestMatchers("/token").permitAll()
        //                         .anyRequest().denyAll())
        //         .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        //         .oauth2ResourceServer(oauth2 -> oauth2
        //                 .jwt(jwt -> jwt
        //                         .jwtAuthenticationConverter(new MyCustomHybridTokenAuthenticationConverter()))); // Adjust the converter to represent your use case
        //                                     // Use MyCustomHybridTokenAuthenticationConverter when IAS and XSUAA is used
        //                                     // Use MyCustomIasTokenAuthenticationConverter when only IAS is used
        // // @formatter:on
        // return http.build();

        return http.securityMatcher(AntPathRequestMatcher.antMatcher("/public/**")).csrf(c -> c.disable()) // don't
                                                                                                           // insist on
                                                                                                           // csrf
                                                                                                           // tokens in
                                                                                                           // put, post
                                                                                                           // etc.
                .authorizeHttpRequests(r -> r.anyRequest().permitAll()).build();
    }

    /**
     * Workaround for hybrid use case until Cloud Authorization Service is globally
     * available.
     */
    // class MyCustomHybridTokenAuthenticationConverter implements Converter<Jwt,
    // AbstractAuthenticationToken>
    // {

    // public AbstractAuthenticationToken convert(Jwt jwt)
    // {
    // if (jwt.hasClaim(TokenClaims.XSUAA.EXTERNAL_ATTRIBUTE))
    // {
    // return authConverter.convert(jwt);
    // }
    // return new AuthenticationToken(jwt, deriveAuthoritiesFromGroup(jwt));
    // }

    // private Collection<GrantedAuthority> deriveAuthoritiesFromGroup(Jwt jwt)
    // {
    // Collection<GrantedAuthority> groupAuthorities = new ArrayList<>();
    // if (jwt.hasClaim(TokenClaims.GROUPS))
    // {
    // List<String> groups = jwt.getClaimAsStringList(TokenClaims.GROUPS);
    // for (String group : groups)
    // {
    // groupAuthorities.add(new SimpleGrantedAuthority(group.replace("IASAUTHZ_",
    // "")));
    // }
    // }
    // return groupAuthorities;
    // }
    // }

    // /**
    // * Workaround for IAS only use case until Cloud Authorization Service is
    // * globally available.
    // */
    // static class MyCustomIasTokenAuthenticationConverter implements
    // Converter<Jwt, AbstractAuthenticationToken>
    // {

    // public AbstractAuthenticationToken convert(Jwt jwt)
    // {
    // final List<String> groups = jwt.getClaimAsStringList(TokenClaims.GROUPS);
    // final List<GrantedAuthority> groupAuthorities = groups == null ?
    // Collections.emptyList()
    // :
    // groups.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    // return new AuthenticationToken(jwt, groupAuthorities);
    // }
    // }

}
