package com.universal.spring.restsecurity

import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UserService(
        private val userRepo: UserRepo
) {

    @Transactional
//    @Cacheable("users")
    fun getUserByEmail(email: String?): User? {
        email ?: return null

        val user = userRepo.findByEmail(email) ?: return null
        user.roles.size

        return user
    }

//    @Transactional
////    @Cacheable("users")
//    fun getUserById(id: Int): User? {
//        val ou = userRepo.findById(id)
//        if (!ou.isPresent) return null
//
//        return ou.get()
//    }
//
//    @Transactional
////    @Cacheable("users")
//    fun getUserByIdWithAuthorities(id: Int): User? {
//        val ou = userRepo.findById(id)
//        if (!ou.isPresent) return null
//
//        val user = ou.get()
//        user.roles.size
//
//        return user
//    }
}