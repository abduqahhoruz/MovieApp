package uz.harmonic.movieapp.common

fun <T> lazyFast(
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
    initializer: () -> T
): Lazy<T> = lazy(mode, initializer)
