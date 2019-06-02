package com.universal.spring.restsecurity

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UserService(
        private val userRepo: UserRepo
) {
    @Transactional
    @Cacheable("users")
    fun getUserByName(name: String?): User? {
        val user = userRepo.findByName(name) ?: return null
        user.roles.size

        return user
    }
}