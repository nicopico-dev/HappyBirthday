package fr.nicopico.happybirthday.extensions

import rx.Observable

fun <T> Observable<T>.ifElse(
        condition: Boolean,
        ifTrue: Observable<T>.() -> Observable<T> = { this },
        ifFalse: Observable<T>.() -> Observable<T> = { this }
) = when (condition) {
    true -> ifTrue()
    false -> ifFalse()
}