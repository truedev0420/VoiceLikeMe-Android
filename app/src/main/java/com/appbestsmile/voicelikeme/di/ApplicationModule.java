package com.appbestsmile.voicelikeme.di;

import android.app.Application;
import android.content.Context;
import dagger.Module;
import dagger.Provides;
import com.appbestsmile.voicelikeme.db.AppDataBase;
import com.appbestsmile.voicelikeme.db.RecordItemDataSource;
import com.appbestsmile.voicelikeme.di.qualifiers.ApplicationContext;
import javax.inject.Singleton;

@Module
public class ApplicationModule {

  @Provides
  @ApplicationContext
  @Singleton
  Context provideApplicationContext(Application application) {
    return application.getApplicationContext();
  }

  @Provides
  @Singleton
  RecordItemDataSource provideRecordItemDataSource(@ApplicationContext Context context) {
    return AppDataBase.getInstance(context).getRecordItemDataSource();
  }
}
