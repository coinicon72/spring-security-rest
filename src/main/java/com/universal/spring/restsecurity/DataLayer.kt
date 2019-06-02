package com.universal.spring.restsecurity

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.*

@Entity
data class Role(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Int = 0,

        var name: String = ""
)

@Entity
data class User(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Int = 0,

        var name: String = "",

        @Column(name = "account", unique = true)
        var email: String = "",

        @JsonIgnore
        @Column(name = "password")
        var pwd: String = "",

        var disabled: Boolean = false,

        @ManyToMany
        @JoinTable(name = "user_role",
                joinColumns = [JoinColumn(name = "id", referencedColumnName = "id")],
                inverseJoinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")]
        )
        val roles: MutableList<Role> = mutableListOf()

) : UserDetails {

    @JsonIgnore
    override fun getUsername(): String = name

    @JsonIgnore
    override fun getPassword(): String = pwd

    @JsonIgnore
    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        return roles.map {
            SimpleGrantedAuthority("ROLE_${it.name}")
        }.toMutableList()
    }

    @JsonIgnore
    override fun isEnabled(): Boolean = !disabled //if (disabled == null) true else !disabled!!

    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean = !disabled //if (disabled == null) true else !disabled!!

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean = !disabled //if (disabled == null) true else !disabled!!

    @JsonIgnore
    override fun isAccountNonLocked(): Boolean = !disabled //if (disabled == null) true else !disabled!!
}


interface UserRepo : JpaRepository<User, Int> {
    fun findByName(name: String?): User?
}