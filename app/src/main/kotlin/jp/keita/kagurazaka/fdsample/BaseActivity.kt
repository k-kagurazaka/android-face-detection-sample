package jp.keita.kagurazaka.fdsample

import android.os.Bundle
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import rx.subjects.Subject

open class BaseActivity : RxAppCompatActivity() {

    private lateinit var error: Subject<String, String>
    private lateinit var subscription: Subscription

    protected var onRxErrorListener: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        error = SerializedSubject(PublishSubject.create<String>())
        subscription = error.observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onRxErrorListener?.invoke(it) })
    }

    override fun onDestroy() {
        subscription.unsubscribe()
        super.onDestroy()
    }

    protected fun <T> subscribeOnMainThread(source: Observable<T>, onNext: (T) -> Unit) {
        source.observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle<T>())
                .subscribe({
                    onNext(it)
                }, {
                    error.onNext(it.message)
                })
    }
}
