package com.matechmatrix.shopflowpos.feature.settings.di

import com.matechmatrix.shopflowpos.feature.settings.data.repository.SettingsRepositoryImpl
import com.matechmatrix.shopflowpos.feature.settings.domain.repository.SettingsRepository
import com.matechmatrix.shopflowpos.feature.settings.presentation.SettingsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    single<SettingsRepository> { SettingsRepositoryImpl(db = get()) }
    viewModel { SettingsViewModel(get()) }
}