package com.example.kalam_android.di.keys

import androidx.lifecycle.ViewModel
import dagger.MapKey

import java.lang.annotation.*
import kotlin.reflect.KClass

//@Documented
@MustBeDocumented
@Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)
