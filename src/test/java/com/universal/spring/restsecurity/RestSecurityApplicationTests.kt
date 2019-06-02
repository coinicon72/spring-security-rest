package com.universal.spring.restsecurity

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.annotation.Cacheable
import org.springframework.test.context.junit4.SpringRunner

//@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RunWith(SpringRunner::class)
@DataJpaTest
class RestSecurityApplicationTests {

	@Autowired
	lateinit var userRepo: UserRepo

//	@Autowired
//	lateinit var userController: UserController

	@Test
	fun jpa() {
//		val users = userController.listUsers()
		val users = userRepo.findAll()

		println(users.size)
	}
}
