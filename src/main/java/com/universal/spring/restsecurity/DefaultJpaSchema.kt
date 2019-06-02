package com.universal.spring.restsecurity

import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable
import javax.persistence.*


//create table users(
//username varchar_ignorecase(50) not null primary key,
//password varchar_ignorecase(50) not null,
//enabled boolean not null
//);
//
//create table authorities (
//username varchar_ignorecase(50) not null,
//authority varchar_ignorecase(50) not null,
//constraint fk_authorities_users foreign key(username) references users(username)
//);
//create unique index ix_auth_username on authorities (username,authority);

@Entity
data class Users(
        @Id
        @Column(length = 50)
        val username: String = "",

        @Column(length = 50)
        val password: String = "",

        val enabled: Boolean = true,

        @OneToMany(mappedBy = "username")
        val authorities: MutableList<Authorities> = mutableListOf()
)

@Entity
data class Authorities (
        @Id
        @Column(length = 50)
        val username: String = "",

        @Id
        @Column(length = 100)
        val authority: String = ""
): Serializable


interface UsersRepo : JpaRepository<Users, String>