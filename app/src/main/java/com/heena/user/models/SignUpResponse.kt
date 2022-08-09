package com.heena.user.models

import java.io.Serializable

data class SignUpResponse(
	val image: String? = null,
	val message: String? = null,
	val status: Int? = null,
	val error: SignUpError?=null,
	val token: String? = null
): Serializable


data class SignUpError(
	var username : ArrayList<String> = arrayListOf(),
	var email : ArrayList<String> = arrayListOf(),
	var phone : ArrayList<String> = arrayListOf()
):Serializable

