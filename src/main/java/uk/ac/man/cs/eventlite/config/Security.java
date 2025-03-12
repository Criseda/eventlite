package uk.ac.man.cs.eventlite.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class Security {

	public static final String ADMIN = "ADMIN";
	public static final String ATTENDEE = "ATTENDEE";
	public static final String ORGANIZER = "ORGANIZER";
	public static final RequestMatcher H2_CONSOLE = antMatcher("/h2-console/**");

	// List the mappings/methods for which no authorisation is required.
	// By default we allow all GETs and full access to the H2 console.
	private static final RequestMatcher[] NO_AUTH = { antMatcher(HttpMethod.GET, "/webjars/**"),
			antMatcher(HttpMethod.GET, "/**"), H2_CONSOLE };

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// By default, all requests are authenticated except our specific list.
		        .authorizeHttpRequests(auth -> auth
		                // Allow static resources and H2 console without authentication
		                .requestMatchers(NO_AUTH).permitAll()
		
		                // Web-based endpoints (/events)
		                .requestMatchers(HttpMethod.GET, "/events/**").permitAll() // Allow anyone to view events
		                .requestMatchers(HttpMethod.POST, "/events").hasAnyRole(ADMIN, ORGANIZER) // Restrict creating events
		                .requestMatchers(HttpMethod.PUT, "/events/**").hasAnyRole(ADMIN, ORGANIZER) // Restrict updating events
		                .requestMatchers(HttpMethod.DELETE, "/events/**").hasAnyRole(ADMIN, ORGANIZER) // Restrict deleting events
		                .requestMatchers(HttpMethod.DELETE, "/events").hasAnyRole(ADMIN, ORGANIZER) // Restrict deleting events
		
		                // API-based endpoints (/api/events)
		                .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll() // Allow anyone to view events via API
		                .requestMatchers(HttpMethod.POST, "/api/events").hasAnyRole(ADMIN, ORGANIZER) // Restrict creating events via API
		                .requestMatchers(HttpMethod.PUT, "/api/events/**").hasAnyRole(ADMIN, ORGANIZER) // Restrict updating events via API
		                .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasAnyRole(ADMIN, ORGANIZER) // Restrict deleting events via API
		                .requestMatchers(HttpMethod.DELETE, "/api/events").hasAnyRole(ADMIN, ORGANIZER) // Restrict deleting events
		
		                // All other requests require authentication
		                .anyRequest().authenticated()
				)
						

				// This makes testing easier. Given we're not going into production, that's OK.
				.sessionManagement(session -> session.requireExplicitAuthenticationStrategy(false))

				// Use form login/logout for the Web.
				.formLogin(login -> login.loginPage("/sign-in").permitAll())
				.logout(logout -> logout.logoutUrl("/sign-out").logoutSuccessUrl("/").permitAll())
				// Use HTTP basic for API
				.httpBasic(withDefaults()) 
	            // Disable CSRF for API endpoints and the H2 console.
	            .csrf(csrf -> csrf.ignoringRequestMatchers(antMatcher("/api/**"), H2_CONSOLE))
	            // Disable frame options to allow the H2 console to display.
	            .headers(headers -> headers.frameOptions(frameOpts -> frameOpts.disable()));

		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		
		UserDetails admin     = User.withUsername("admin").password(encoder.encode("admin")).roles(ADMIN).build();
		UserDetails organizer = User.withUsername("organizer").password(encoder.encode("organizer")).roles(ORGANIZER).build();
		UserDetails attendee  = User.withUsername("attendee").password(encoder.encode("attendee")).roles(ATTENDEE).build();
		UserDetails rob       = User.withUsername("Rob").password(encoder.encode("Haines")).roles(ADMIN, ORGANIZER).build();
		UserDetails caroline  = User.withUsername("Caroline").password(encoder.encode("Jay")).roles(ADMIN).build();
		UserDetails markel    = User.withUsername("Markel").password(encoder.encode("Vigo")).roles(ADMIN).build();
		UserDetails mustafa   = User.withUsername("Mustafa").password(encoder.encode("Mustafa")).roles(ADMIN).build();
		UserDetails tom       = User.withUsername("Tom").password(encoder.encode("Carroll")).roles(ATTENDEE).build();

		return new InMemoryUserDetailsManager(admin, organizer, attendee, rob, caroline, markel, mustafa, tom);
	}
}
