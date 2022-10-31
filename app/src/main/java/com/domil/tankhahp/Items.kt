package com.domil.tankhahp

data class Items(
    var date: String,
    var specification: String,
    var payTo: String,
    var factorNumber: Int,
    var price: Long,
    var imgAddress : String,
    var hasImageFile : Boolean
)
