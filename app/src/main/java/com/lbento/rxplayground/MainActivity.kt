package com.lbento.rxplayground

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.jakewharton.rxbinding2.view.RxView.clicks
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Thread.currentThread
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clicks(activity_main_button1 as Button).subscribe { rxCreatingSimpleObservable() }

        clicks(activity_main_button2 as Button).subscribe { rxHotConnectableObservable() }
        clicks(activity_main_button3 as Button).subscribe { rxHotShareObservable() }

        clicks(activity_main_button4 as Button).subscribe { rxMappingObservable() }
        clicks(activity_main_button5 as Button).subscribe { rxFlatMappingObservable() }
        clicks(activity_main_button6 as Button).subscribe { rxFlatMappingObservableChanging() }
        clicks(activity_main_button7 as Button).subscribe { rxThreadMappingObservable() }
        clicks(activity_main_button8 as Button).subscribe { rxThreadFlatMappingObservable() }


        clicks(activity_main_button9 as Button).subscribe { rxFilterObservable() }
        clicks(activity_main_button10 as Button).subscribe { rxDebounceObservable() }

        clicks(activity_main_button11 as Button).subscribe { rxComposeLikeObservable() }

    }


    private fun rxCreatingSimpleObservable() {
        myIntObservable.subscribe {
            Log.d(MainActivity::class.java.name, it.toString())
        }
    }


    /*

    SLIDE 5

     */

    private fun rxHotConnectableObservable() {
        val textFieldObservable = Observable.interval(1L, SECONDS).map(Long::toString).publish()

        textFieldObservable.subscribe { Log.d(MainActivity::class.java.name, "Observer1 : $it") }

        sleep(3000)

        textFieldObservable.subscribe { Log.d(MainActivity::class.java.name, "Observer2 : $it") }

        textFieldObservable.connect()
    }

    /*

    SLIDE 5

    */

    private fun rxHotShareObservable() {
        val textFieldObservable = Observable.interval(1L, SECONDS).map(Long::toString).share() //similar to autoConnect but it
        //disposes itself after there are no more subscriptions - starts over on the next subscription

        textFieldObservable.subscribe { Log.d(MainActivity::class.java.name, "Observer1 : $it") }

        sleep(3000)

        textFieldObservable.subscribe { Log.d(MainActivity::class.java.name, "Observer2 : $it") }
    }


    /*

    SLIDE 7

     */
    private fun rxMappingObservable() {
        Observable.fromArray(1, 2, 3, 4, 5)
                .map(Int::toString)
                .subscribe {
                    Log.d(MainActivity::class.java.name, it) //avoids it.toString()
                }
    }

    /*

    SLIDE 7 - equivalent to mapping - SHOULD NOT BE USED LIKE THIS - another observable needs to be created
    If flatmap is returning a just, something is wrong.

     */
    private fun rxFlatMappingObservable() {
        Observable.fromArray(1, 2, 3, 4, 5)
                .flatMap { Observable.just("Number $it") }
                .subscribe {
                    Log.d(MainActivity::class.java.name, it)
                }
    }

    /*

    SLIDE 7 - flatMap changing stream

     */
    private fun rxFlatMappingObservableChanging() {
        Observable.fromArray(1, 2)
                .flatMapSingle {
                    Observable.fromArray("A", "B", "C", "D", "E").collect(
                            { mutableListOf<String>() },
                            { list, element -> list.add(element) })
                }
                .subscribe {
                    Log.d(MainActivity::class.java.name, it.toString())
                }
    }


    /*

    SLIDE 8

    */
    private fun rxThreadMappingObservable() {
        Observable.fromArray(1, 2, 3, 4, 5)
                .subscribeOn(Schedulers.io())
                .doOnEach {
                    Log.d("Thread-doOnEach", currentThread().name)
                }
                .map {
                    Log.d("Thread-map", currentThread().name)
                    "Number $it"
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d("Thread-subscribe", currentThread().name)
                    Log.d(MainActivity::class.java.name, it)
                }
    }

    /*

    SLIDE 8

     */
    private fun rxThreadFlatMappingObservable() {
        Observable.fromArray(1, 2, 3, 4, 5)
                .doOnEach {
                    Log.d("Thread-doOnEach", currentThread().name)
                }
                .flatMap {
                    Log.d("Thread-flatMap", currentThread().name)
                    Observable.just("Number $it")
                }
                .subscribe {
                    Log.d("Thread-subscribe", currentThread().name)
                    Log.d(MainActivity::class.java.name, it)
                }
    }


    /*

    SLIDE 9

     */

    private fun rxFilterObservable() {
        Observable.fromArray(1, 2, 3, 4, 5)
                .filter { it > 2 }
                .reduce { sum, number -> sum + number }
                .map { it.toString() }
                .applySchedulers()
                .subscribe {
                    Log.d(MainActivity::class.java.name, it)
                }
    }


    private fun rxDebounceObservable() {
        Observable.interval(1000, MILLISECONDS, Schedulers.computation())
                .take(10)
                .map(Long::toString)
                .debounce(1000, MILLISECONDS)
                .subscribe {
                    Log.d(MainActivity::class.java.name, it)
                }
    }

    /*

    SLIDE 11

     */
    private fun rxComposeLikeObservable() {
        Observable.fromArray(1, 2, 3, 4, 5)
                .doOnEach {
                    Log.d("Thread-doOnEach", currentThread().name)
                }
                .flatMap {
                    Log.d("Thread-flatMap", currentThread().name)
                    Observable.just("Number $it")
                }
                .applySchedulers()
                .subscribe {
                    Log.d("Thread-subscribe", currentThread().name)
                    Log.d(MainActivity::class.java.name, it)
                }
    }

    private fun <T> Observable<T>.applySchedulers(): Observable<T> {
        return subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
    }

    private fun <T> Maybe<T>.applySchedulers(): Maybe<T> {
        return subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
    }

    private val myIntObservable = Observable.create<Int> {
        it.onNext(1)
        it.onNext(2)
        it.onNext(3)
        it.onComplete()
    }


    private fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: Exception) {
        }
    }

}
