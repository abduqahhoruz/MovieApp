package uz.harmonic.movieapp.util

sealed class NetworkStatus {
    object LOADING : NetworkStatus()
    object SUCCESS : NetworkStatus()
    class ERROR(val res: Int) : NetworkStatus()
}