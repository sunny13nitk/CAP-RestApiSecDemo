package restapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.sap.cloud.security.spring.config.IdentityServicesPropertySourceFactory;

import restapi.utilities.CL_DestinationUtilities;

@Configuration
@Profile(CL_DestinationUtilities.GC_LocalProfile)
@EnableWebSecurity()
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@PropertySource(factory = IdentityServicesPropertySourceFactory.class, ignoreResourceNotFound = true, value =
{ "" })
public class AppSecurityConfigLocal
{

    @Bean
    @Profile(CL_DestinationUtilities.GC_LocalProfile)
    public SecurityFilterChain appFilterChain(HttpSecurity http) throws Exception
    {
        // @formatter:off
        http
               .authorizeHttpRequests(authz ->
                           authz
                                .requestMatchers("/authorize/**").permitAll()
                                .requestMatchers("/logs/").hasAuthority("RESTREAD")
                                .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) ; 
        // Adjust the converter to represent your use case
        // Use MyCustomHybridTokenAuthenticationConverter when IAS and XSUAA is used
        // Use MyCustomIasTokenAuthenticationConverter when only IAS is used                                
        // @formatter:on
        return http.build();

    }

    /*
     * WEB REsources Whitelisting
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() throws Exception
    {
        // @formatter:off
        return (web) -> web.ignoring()
                    .requestMatchers("/static/**")
                    .requestMatchers("/images/**")
                    .requestMatchers("/css/**")
                    .requestMatchers("/js/**")
                    .requestMatchers("/logout/**");
        // @formatter:on
    }

}
