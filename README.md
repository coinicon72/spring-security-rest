## gradle
```kotlin
implementation("org.springframework.boot:spring-boot-starter-web")  
implementation("org.springframework.boot:spring-boot-starter-security")
```

## 基本配置
一旦`spring-boot-starter-security`配置在class path中，无需任何其他设置，即启用了缺省配置。

### HttpSecurity
参考源文件`org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure`

```java
protected void configure(HttpSecurity http) throws Exception {  
  http  
      .authorizeRequests()  
         .anyRequest().authenticated()  
         .and()  
      .formLogin().and()  
      .httpBasic();  
}
```

即：
+ 使用内置登录、注销机制
+ 登录页面`/login`
+ 注销页面`/logout`
+ 支持Http Basic Authentication
+ 所有其他请求要求登录

### UserDetailsService
缺省的用户只有一个，用户名`user`，密码在启动时自动生成：

```
Using generated security password: d9ef0cdd-7a1a-4cee-8d1e-1e4eca5c17cf
```

可参考：
`org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration#inMemoryUserDetailsManager`
`org.springframework.boot.autoconfigure.security.SecurityProperties.User`

### PasswordEncoder
缺省为`org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter.LazyPasswordEncoder`

参考：
`org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#setApplicationContext`
`org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter.LazyPasswordEncoder`

## 基于数据库的UserDetailsService
```kotlin
@Service  
class JdbcUserDetailsService(  
        val userService: UserService  
) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {  
        return userService.getUserByEmail(username) ?: throw UsernameNotFoundException("$username not found")  
    }  
}
```
其中`UserService`为数据访问服务。

## 自定义PasswordEncoder
如下是一个自定义的、支持MD5加密的Encoder
```kotlin
@Service  
@ConditionalOnMissingBean(type = ["passwordEncoder"])  
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
```

## 支持token-based-authentication
基于token的认证方式，和前面基于form-login的方式有较大不同：
+ HttpSecurity中禁用formlogin，开放`token接口`
+ 用户显式请求`token接口`来获得生成的`token`
+ 在后续的请求中，在http头信息"Authorization"中包含的`token`，如：`Authorization: Bearer 4e60344c-6ea8`
+ 使用filter拦截请求，并检测"Authorization"头信息中包含的`token`
+ 检查`token`是否有效、是否过期
+ 自行加载用户的权限信息，并注入SecurityContextHolder

示例代码使用一个简单的内存Map存储`token`：
```kotlin
var tokenCache: MutableMap<String, Token>
```

### 设置HttpSecurity
```kotlin
        http  
//                .httpBasic().and() // disable BASIC authentication 'cuz we want force client use token  
            .authorizeRequests()  
            .antMatchers("/token").permitAll()  
//                .antMatchers("/login").permitAll()  
			.anyRequest().authenticated()  
  
            .and().logout()  
  
            .and().csrf().disable() // POST method of '/token' with curl will failed when CSRF enabled  
```

### 配置filter
```kotlin
@EnableWebSecurity  
@EnableGlobalMethodSecurity(prePostEnabled = true)  
class SecurityConfig(  
        private val tokenAuthenticationFilter: TokenAuthenticationFilter  
) : WebSecurityConfigurerAdapter() {  
  
    override fun configure(http: HttpSecurity) {  
        //Implementing Token based authentication in this filter  
        http.addFilterBefore(tokenAuthenticationFilter, BasicAuthenticationFilter::class.java)  
  
        //  
        http  
//                .httpBasic().and() // disable BASIC authentication 'cuz we want force client use token  
            .authorizeRequests()  
            .antMatchers("/token").permitAll()  
//                .antMatchers("/login").permitAll()  
			.anyRequest().authenticated()  
  
            .and().logout()  
  
            .and().csrf().disable() // POST method of '/token' with curl will failed when CSRF enabled  
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
                        val authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)  
                        SecurityContextHolder.getContext().authentication = authentication  
                    }  
                }  
            }  
        }  
  
        chain.doFilter(request, response)  
    }  
}
```

### 提供`token接口`
```kotlin
/**  
 * rest controller to generate token after verify credential successfully * * the token will store in tokenCache for further usage */@RestController  
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
		// generate random token string  
		val randomString = UUID.randomUUID().toString()  
  
	    // create token  
		val token = Token(randomString, user, 
		                  Instant.now().plus(appProperties.token.expire, ChronoUnit.SECONDS))  
  
        // save it  
	    tokenCache[randomString] = token  
  
        //  
        return token  
    }  
}
```
