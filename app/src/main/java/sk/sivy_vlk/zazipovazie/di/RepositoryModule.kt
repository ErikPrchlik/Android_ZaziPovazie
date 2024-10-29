package sk.sivy_vlk.zazipovazie.di

import org.koin.dsl.module
import sk.sivy_vlk.zazipovazie.repository.IKMZInputStreamRepository
import sk.sivy_vlk.zazipovazie.repository.KMZInputStreamRepositoryImpl

val KMZInputStreamRepositoryModule = module {
    single { provideKMZInputStreamRepository() }
}

fun provideKMZInputStreamRepository(): IKMZInputStreamRepository {
    return KMZInputStreamRepositoryImpl()
}