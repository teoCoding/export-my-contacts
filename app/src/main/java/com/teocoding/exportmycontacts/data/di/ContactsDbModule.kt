package com.teocoding.exportmycontacts.data.di

import com.teocoding.exportmycontacts.data.db.PhoneBookDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class ContactsDbModule {

    @Singleton
    @Provides
    fun providesContactsDb() = PhoneBookDb.realm
}