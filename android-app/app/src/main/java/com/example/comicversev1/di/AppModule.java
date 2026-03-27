package com.example.comicversev1.di;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.comicversev1.utils.Constants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(@ApplicationContext Context context) {
        return context.getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
    }
}

