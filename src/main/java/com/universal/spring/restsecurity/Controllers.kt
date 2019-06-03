package com.universal.spring.restsecurity

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal


/**
 * normal rest service
 */
@RestController
@RequestMapping(path = ["/user"])
class UserController(
//        val userService: UserService
) {

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