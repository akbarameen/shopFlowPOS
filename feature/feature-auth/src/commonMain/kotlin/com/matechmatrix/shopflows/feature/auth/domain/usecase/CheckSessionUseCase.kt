package com.matechmatrix.shopflows.feature.auth.domain.usecase

import com.matechmatrix.shopflows.feature.auth.domain.repository.AuthRepository

class CheckSessionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Boolean = repository.isActivated()
}