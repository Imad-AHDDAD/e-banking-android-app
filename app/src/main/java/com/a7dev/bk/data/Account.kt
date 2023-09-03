package com.a7dev.bk.data

data class Account(
    val accountNumber: Long,
    val user: User,
    val balance: Float
){
    constructor(): this(0,User("","","",""),0f)
}