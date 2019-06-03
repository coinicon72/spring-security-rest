package com.universal.spring.restsecurity

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.temporal.ChronoUnit

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

//    @Value("${app.token.length}")
//    var tokenLength: Int = 16
//
//    @Value("${app.token.expire}")
//    var tokenExpire: Long = 3600
    @Autowired
    lateinit var appProperties: AppProperties


    @PostMapping
    fun createToken(uid: String?, pwd: String?): ResponseEntity<Token> {
        uid ?: return ResponseEntity(Token(), HttpStatus.BAD_REQUEST)
        pwd ?: return ResponseEntity(Token(), HttpStatus.BAD_REQUEST)

        // check credential here
        val user = userService.getUserByEmail(uid) ?: return ResponseEntity(Token(), HttpStatus.UNAUTHORIZED)
        if (MD5Util.toMD5(pwd) != user.pwd)
            return ResponseEntity(Token(), HttpStatus.UNAUTHORIZED)

        // generate token and return
        val token = generateAndStoreToken(user)
        return ResponseEntity.ok(token)
    }


    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    /**
     * generate token and store it
     */
    private fun generateAndStoreToken(user: User): Token {
        //TODO should consider whether reuse exist token associate with user, or always create new token?

        // generate random token string
        var randomString = generateRandomString()
        while (tokenCache.containsKey(randomString)) {
            println("token collided, re-creating")
            randomString = generateRandomString()
        }

        // create token
        val token = Token(randomString, user, Instant.now().plus(appProperties.token.expire, ChronoUnit.SECONDS))

        // save it
        tokenCache[randomString] = token

        //
        return token
    }

    private fun generateRandomString() = (1..appProperties.token.length)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
}
