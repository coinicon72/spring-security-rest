package com.universal.spring.restsecurity

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.cache.support.SimpleCacheManager
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
import org.springframework.stereotype.Service
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.transaction.Transactional


@Configuration
@EnableCaching
class CachingConfig {
    @Bean
    fun cacheManager(): CacheManager {
//        return ConcurrentMapCacheManager ("tokens");

        val cacheManager = SimpleCacheManager()
        cacheManager.setCaches(listOf(
                ConcurrentMapCache("tokens"),
                ConcurrentMapCache("users")))

        return cacheManager;
    }
}

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
//class WebSecurityConfig: WebMvcConfigurer {

//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    fun passwordEncoder(): PasswordEncoder {
//        return NoOpPasswordEncoder.getInstance()
//
////        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
//    }
//}

class SecurityConfig : WebSecurityConfigurerAdapter() {

//    @Throws(Exception::class)
//    public override fun configure(auth: AuthenticationManagerBuilder) {
//        auth.userDetailsService<UserDetailsService>(this.participantService).passwordEncoder(this.passwordEncoder())
//    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {

        //Implementing Token based authentication in this filter
        val tokenFilter = TokenAuthenticationFilter()
        http.addFilterBefore(tokenFilter, BasicAuthenticationFilter::class.java)

//        //Creating token when basic authentication is successful and the same token can be used to authenticate for further requests
//        val customBasicAuthFilter = CustomBasicAuthenticationFilter(this.authenticationManager())
//        http.addFilter(customBasicAuthFilter)

        http.csrf().and()
                .cors().and()
                .antMatcher("/token").anonymous()
    }
}


class TokenAuthenticationFilter : GenericFilterBean() {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest

        //extract token from header
        val accessToken = httpRequest.getHeader("Authorization")

        if (null != accessToken) {
            //get and check whether token is valid ( from DB or file wherever you are storing the token)

            //Populate SecurityContextHolder by fetching relevant information using token
            val user = org.springframework.security.core.userdetails.User(
                    "username",
                    "password",
                    true,
                    true,
                    true,
                    true,
                    null)

            val authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)
            SecurityContextHolder.getContext().authentication = authentication
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

    @Transactional
    override fun loadUserByUsername(username: String?): UserDetails {
        return userService.getUserByName(username) ?: throw UsernameNotFoundException("$username not found")
    }
}


@ConditionalOnMissingBean(type = ["passwordEncoder"])
@Service
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
