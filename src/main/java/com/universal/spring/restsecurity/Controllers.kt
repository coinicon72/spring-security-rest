package com.universal.spring.restsecurity

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping(path = ["/token"])
class TokenController {
    @PostMapping
    fun createToken(uid: String?, pwd: String?): ResponseEntity<String> {
        uid ?: return ResponseEntity("", HttpStatus.BAD_REQUEST)
        pwd ?: return ResponseEntity("", HttpStatus.BAD_REQUEST)

        val token = generateToken(uid, pwd)

        return ResponseEntity.ok(token)
    }


    @Autowired
    lateinit var tokenCache: MutableMap<String, String>

    /**
     * generate token and store it
     */
    private fun generateToken(uid: String, pwd: String): String {
        var existToken: String? = null

        tokenCache.filter {
            it.value == uid
        }.forEach { (t, _) -> existToken = t }

        if (existToken == null) {
            existToken = "abc.$uid"
            tokenCache[existToken!!] = uid
        }

        return existToken!!
    }
}

@RestController
@RequestMapping(path = ["/user"])
class UserController(
//        val userService: UserService
) {

    /**
     * user   --  role/group (a set of authorities)  --   authority
     */

    @GetMapping
    fun listUsers(principal: Principal): List<User> {
//        val users = userRepo.findAll()
//
//        users.forEach {
//            it.roles.size
//        }
//
//        return users

        return listOf(User(1, principal.name))
    }

    @PreAuthorize("hasRole('ROLE_企业管理员')")
//    @PreAuthorize("hasAuthority('企业管理员')")
    @GetMapping("/admin")
    fun listUsersByAdmin(principal: Principal): List<User> {
//        val users = userRepo.findAll()
//
//        users.forEach {
//            it.roles.size
//        }
//
//        return users

        return listOf(User(1, principal.name))
    }
}