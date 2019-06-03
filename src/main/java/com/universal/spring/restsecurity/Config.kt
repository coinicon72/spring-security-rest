package com.universal.spring.restsecurity

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.filter.GenericFilterBean
import java.time.Instant
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest


@Configuration
@EnableCaching
class CachingConfig {
//    @Bean
//    fun cacheManager(): CacheManager {
////        return ConcurrentMapCacheManager ("tokens");
//
//        val cacheManager = SimpleCacheManager()
//        cacheManager.setCaches(listOf(
//                ConcurrentMapCache("tokens"),
//                ConcurrentMapCache("users")))
//
//        return cacheManager;
//    }

    //TODO token should associate with more information, like expire
    @Bean
    fun tokenCache(): MutableMap<String, Token> = HashMap()

//    @Bean
//    fun passwordEncoder(): PasswordEncoder {
//        return NoOpPasswordEncoder.getInstance()
//
////        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
//    }
}

data class Token (
        val token: String = "",

        /** any information to identify user.
         * @see com.universal.spring.restsecurity.User
         */
        val user: User = User(),

        /** token expire time, will expired beyond this point */
        val expire: Instant = Instant.now()
) {
    fun isExpired(): Boolean = Instant.now().isAfter(expire)

    override fun toString(): String {
        return "<Token [token: $token, user: $user, expire: $expire]>"
    }
}


@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig(
        private val tokenAuthenticationFilter: TokenAuthenticationFilter
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {

        //Implementing Token based authentication in this filter
        http.addFilterBefore(tokenAuthenticationFilter, BasicAuthenticationFilter::class.java)

//        //Creating token when basic authentication is successful and the same token can be used to authenticate for further requests
//        val customBasicAuthFilter = CustomBasicAuthenticationFilter(this.authenticationManager())
//        http.addFilter(customBasicAuthFilter)

        //
        http
//                .httpBasic().and() // disable BASIC authentication 'cuz we want force client use token
                .authorizeRequests()
                .antMatchers("/token").permitAll()
//                .antMatchers("/login").permitAll()
                .anyRequest().authenticated()

                .and().logout()

                .and().csrf().disable() // POST method of '/token' with curl will failed when CSRF enabled
//                .and().cors()
    }
}


@Component("tokenAuthenticationFilter")
class TokenAuthenticationFilter(
        private val tokenCache: MutableMap<String, Token>,
//        private val userService: UserService
        private val userDetailsService: UserDetailsService
) : GenericFilterBean() {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest

        //extract token from header
        var tokenString: String? = httpRequest.getHeader("Authorization")

        // support with or without 'Bearer ' prefix
        tokenString = tokenString?.removePrefix("Bearer ")


        // validate token
        if (null != tokenString) {

            val token = tokenCache[tokenString]
            if (token != null) {

                if (token.isExpired()) {
                    println("removing expired token: $token")
                    tokenCache.remove(tokenString)
                } else {

                    // use any method to load user authorities
//                    val user = userService.getUserByEmail(token.user.email)
                    val user = userDetailsService.loadUserByUsername(token.user.email)

                    if (user != null) {
                        // inject to SecurityContext
                        val authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            }
        }

        chain.doFilter(request, response)
    }
}


//@Component
//class CustomBasicAuthenticationFilter (authenticationManager: AuthenticationManager)
//    : BasicAuthenticationFilter(authenticationManager) {
//
//    protected override fun onSuccessfulAuthentication(request: HttpServletRequest,
//                                                      response: HttpServletResponse,
//                                                      authResult: Authentication) {
//        //Generate Token
//        //Save the token for the logged in user
//        //send token in the response
//        response.setHeader("header-name", "token")
//    }
//}


@Service
class JdbcUserDetailsService(
        val userService: UserService
) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        return userService.getUserByEmail(username) ?: throw UsernameNotFoundException("$username not found")
    }
}


@Service
@ConditionalOnMissingBean(type = ["passwordEncoder"])
//@ConditionalOnMissingBean(PasswordEncoder::class)
class MD5PasswordEncoder : PasswordEncoder {
    override fun encode(rawPassword: CharSequence?): String {
        rawPassword ?: return ""

        return MD5Util.toMD5(rawPassword)
    }

    override fun matches(rawPassword: CharSequence?, encodedPassword: String?): Boolean {
        rawPassword ?: return false
        encodedPassword ?: return false

        return encodedPassword.equals(MD5Util.toMD5(rawPassword), true)
    }
}

@ConfigurationProperties(prefix="app")
@Component
//@Validated
data class AppProperties (
	var token: TokenSettings = TokenSettings()
)


//@ConfigurationProperties(prefix="app.token")
data class TokenSettings (
        var length: Int = 16,

        var expire: Long = 3600
)

