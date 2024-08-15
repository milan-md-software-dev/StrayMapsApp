package com.example.straymaps.data

/** This class represents the user of the app. If the user has an anonymous account,
 *  then "isAnonymous" variable is true. If they later wish to turn their anonymous account
 *  into an actual account, "isAnonymous" will be false
 */
data class User(
    val id: String = "",
    val email: String = "",
    val provider: String = "",
    val displayName: String = "",
    val isAnonymous: Boolean? = null
)
