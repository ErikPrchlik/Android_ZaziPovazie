package sk.sivy_vlk.zazipovazie.di

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import sk.sivy_vlk.zazipovazie.view_model.MapActivityViewModel

val appModule = module {
    viewModel { MapActivityViewModel(androidApplication(), get()) }
}