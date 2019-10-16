package com.example.kalam_android.di.modules

import androidx.lifecycle.ViewModel
import com.example.kalam_android.di.keys.ViewModelKey
import com.example.kalam_android.viewmodel.LoginViewModel
import com.example.kalam_android.viewmodel.SignUpViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindSignInViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    abstract fun bindSignUpViewModel(viewModel: SignUpViewModel): ViewModel

}