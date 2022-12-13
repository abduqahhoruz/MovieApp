package uz.harmonic.movieapp.data.prefs

interface Prefs {
    var downloads: String?

    companion object{
        const val PREF_NAME = "movieApp"
        const val DOWNLOADS ="DOWNLOADS"
    }
}
