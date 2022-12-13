package uz.harmonic.movieapp.data.prefs

import android.content.SharedPreferences

class PrefsImpl constructor(
    private val prefs: SharedPreferences
) :Prefs{
    override var downloads: String?
        get() = prefs.getString(Prefs.DOWNLOADS,"").apply {  }
        set(value) {
            prefs.edit().putString(Prefs.DOWNLOADS,value).apply()
        }
}