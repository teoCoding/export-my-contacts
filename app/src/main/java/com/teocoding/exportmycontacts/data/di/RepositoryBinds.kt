package com.teocoding.exportmycontacts.data.di

import com.teocoding.exportmycontacts.data.repository.ContactRepositoryImpl
import com.teocoding.exportmycontacts.domain.repository.ContactRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBinds {

    @Binds
    @Singleton
    abstract fun bindsContactsRepository(contactsRepositoryImpl: ContactRepositoryImpl): ContactRepository
}