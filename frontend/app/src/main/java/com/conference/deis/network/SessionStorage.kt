package com.conference.deis.network

import android.content.Context
import com.conference.deis.network.model.LoginResponse
import com.google.gson.Gson

object SessionStorage {
    private const val PREFS = "deis_session"
    private const val KEY_USER = "user_json"

    fun guardarUsuario(context: Context, user: LoginResponse) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER, Gson().toJson(user)).apply()
    }

    fun restaurarUsuario(context: Context): LoginResponse? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_USER, null) ?: return null

        return try {
            Gson().fromJson(json, LoginResponse::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun limpiar(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_USER).apply()
    }
}