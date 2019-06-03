package com.universal.spring.restsecurity

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * rest controller to generate token after verify credential successfully
 *
 * the token will store in tokenCache for further usage
 */
@RestController
@RequestMapping(path = ["/token"])
class TokenController(
        private val userService: UserService
) {
    @Autowired
    lateinit var tokenCache: MutableMap<String, Token>

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var appProperties: AppProperties


    @PostMapping
    fun createToken(uid: String?, pwd: String?): ResponseEntity<Token> {
        uid ?: return ResponseEntity(Token(), HttpStatus.BAD_REQUEST)
        pwd ?: return ResponseEntity(Token(), HttpStatus.BAD_REQUEST)

        // check credential here
        val user = userService.getUserByEmail(uid) ?: return ResponseEntity(Token(), HttpStatus.UNAUTHORIZED)
        if (!passwordEncoder.matches(pwd, user.pwd))
            return ResponseEntity(Token(), HttpStatus.UNAUTHORIZED)

        // generate token and return
        val token = generateAndStoreToken(user)
        return ResponseEntity.ok(token)
    }


    /**
     * generate token and store it
     */
    private fun generateAndStoreToken(user: User): Token {
        //TODO should consider whether reuse exist token associate with user, or always create new token?

        // generate random token string
        val randomString = UUID.randomUUID().toString()

        // create token
        val token = Token(randomString, user, Instant.now().plus(appProperties.token.expire, ChronoUnit.SECONDS))

        // save it
        tokenCache[randomString] = token

        //
        return token
    }
}
