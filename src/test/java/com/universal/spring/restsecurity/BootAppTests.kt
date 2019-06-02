package com.universal.spring.restsecurity

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
class BootAppTests {

	@Test
	fun testCaching() {
		println("kevin => ${getToken("kevin")}")
		println("jeff => ${getToken("jeff")}")
		println("kevin => ${getToken("kevin")}")
		println("jeff => ${getToken("jeff")}")
	}


	@Cacheable("tokens")
	fun getToken(name: String): String {
		println("create token for $name")
		return MD5Util.toMD5(name)
	}
}
