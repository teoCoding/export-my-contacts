package com.teocoding.exportmycontacts.data.di

import com.teocoding.exportmycontacts.data.file.PdfCacheServiceImpl
import com.teocoding.exportmycontacts.domain.PdfCacheService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class PdfCacheServiceBinds {

    @Binds
    @ViewModelScoped
    abstract fun bindsPdfCacheService(pdfCacheServiceImpl: PdfCacheServiceImpl): PdfCacheService
}