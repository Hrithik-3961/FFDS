package com.codechef.ffds

import android.app.Application
import java.util.concurrent.Executors

class UserRepository(application: Application) {

    private var userDatabase: UserDatabase = UserDatabase.getInstance(application)
    private var userDao: UserDao = userDatabase.noteDao()

    private val executorService = Executors.newSingleThreadExecutor()

    fun insert(user: Profile) = executorService.execute { userDao.insert(user) }

    fun update(user: Profile) = executorService.execute { userDao.update(user) }

    fun getUserData():Profile = userDao.getUserData()

    fun clear() = executorService.execute { userDao.clear() }
}