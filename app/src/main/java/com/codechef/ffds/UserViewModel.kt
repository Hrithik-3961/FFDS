package com.codechef.ffds

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private var repository = UserRepository(application)

    fun insert(user: Profile) = repository.insert(user)

    fun update(user: Profile) = repository.update(user)

    fun getUserData(): Profile = repository.getUserData()

    fun clear() = repository.clear()
}