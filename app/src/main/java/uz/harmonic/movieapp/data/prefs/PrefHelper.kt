package uz.harmonic.movieapp.data.prefs

import android.content.Context

object PrefHelper {

    private var prefs: Prefs? = null

    fun getPref(context: Context): Prefs {
        if (prefs == null) {
            prefs = PrefsImpl(
                context.getSharedPreferences(
                    Prefs.PREF_NAME,
                    Context.MODE_PRIVATE
                )
            )
        }
        return prefs!!
    }
}