package jp.keita.kagurazaka.fdsample

import rx.Observable

fun <T> createPromise(generator: () -> T) = Observable.create<T> {
    try {
        it.onNext(generator())
        it.onCompleted()
    } catch (e: Exception) {
        it.onError(e)
    }
}
