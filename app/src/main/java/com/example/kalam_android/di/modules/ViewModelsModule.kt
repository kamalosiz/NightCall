package com.example.kalam_android.di.modules

import androidx.lifecycle.ViewModel
import com.example.kalam_android.di.keys.ViewModelKey
import com.example.kalam_android.viewmodel.*
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

    @Binds
    @IntoMap
    @ViewModelKey(VerifyCodeViewModel::class)
    abstract fun bindVerifyCodeViewModel(viewModel: VerifyCodeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateProfileViewModel::class)
    abstract fun bindCreateProfileViewModel(viewModel: CreateProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactsViewModel::class)
    abstract fun bindContactsViewModel(viewModel: ContactsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AllChatListViewModel::class)
    abstract fun bindAllChatListViewModel(viewModel: AllChatListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatMessagesViewModel::class)
    abstract fun bindChatMessagesViewModel(viewModel: ChatMessagesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FindFriendsViewModel::class)
    abstract fun bindFindFriendsViewModel(viewModel: FindFriendsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LogoutViewModel::class)
    abstract fun bindLogoutViewModel(viewModel: LogoutViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(UserProfileViewModel::class)
    abstract fun bindUserProfileViewModel(viewModel: UserProfileViewModel): ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(EditUserProfileViewModel::class)
    abstract fun bindEditUserProfileViewModel(viewModel: EditUserProfileViewModel): ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(ForgetPasswordViewModel::class)
    abstract fun bindForgetPasswordViewModel(viewModel: ForgetPasswordViewModel): ViewModel
}