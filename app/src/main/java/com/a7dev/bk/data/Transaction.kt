package com.a7dev.bk.data

data class Transaction(
    val accountToDebit: Account,
    val accountToCreditNumber: String,
    val amount: String,
    val motif: String,
    val date: String,

    ) {
    constructor() : this(
        Account(0, User("", "", "", ""), 0f), "",
        "",
        "",
        ""
    )
}