package com.codechef.ffds

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.codechef.ffds.databinding.LoginActivityBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private val viewModel = ViewModelProvider(this, UserViewModelFactory(application)).get(UserViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://ffds-new.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiHolder=retrofit.create(ApiHolder::class.java)

        binding.apply {
            createNew.setOnClickListener {
                startActivity(
                    Intent(
                        baseContext,
                        RegisterActivity1::class.java
                    )
                )
            }

            loginBtn.setOnClickListener {
                val email = emailInput.text.toString().trim()
                val password = passInput.text.toString().trim()
                if (email.isEmpty() || password.isEmpty()) {
                    prompt.text = "* Fields can't be empty"
                    prompt.visibility = View.VISIBLE
                } else if (!email.contains("vitstudent.ac.in")) {
                    prompt.text = "* Enter a valid VIT email"
                    prompt.visibility = View.VISIBLE
                } else
                //loginUser(email, password, apiHolder)
                    startActivity(Intent(baseContext, MainActivity::class.java))
            }
        }
    }

    fun loginUser(email:String, password:String, apiHolder:ApiHolder){

        val fields= mutableMapOf("email" to email, "password" to password)

        Api.retrofitService.login(fields)!!.enqueue(object: Callback<Token?> {
            override fun onFailure(call: Call<Token?>, t: Throwable) {
                Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Token?>, response: Response<Token?>) {
                Toast.makeText(baseContext, response.message(), Toast.LENGTH_SHORT).show()
                val token= response.body()?.token
                if(response.message()=="OK") {
                    if (token != null) {
                        viewModel.insert(Profile(token = token))
                        updateProfile(token, apiHolder, email)
                    }
                    startActivity(Intent(baseContext, MainActivity::class.java))
                    finish()
                }
            }
        })
    }

    fun updateProfile(token: String, apiHolder: ApiHolder, email: String){
        Api.retrofitService.profileView("JWT $token", email)?.enqueue(object: Callback<ProfileResponse?>{
            override fun onFailure(call: Call<ProfileResponse?>, t: Throwable) {
                Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ProfileResponse?>, response: Response<ProfileResponse?>) {
                Toast.makeText(baseContext, response.body()!!.user.name, Toast.LENGTH_SHORT).show()
                if(response.message()=="OK"){
                    val user:Profile= response.body()!!.user
                    viewModel.update(user)
                }
            }
        })
    }
}
