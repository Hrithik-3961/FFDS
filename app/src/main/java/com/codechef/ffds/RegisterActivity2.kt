package com.codechef.ffds

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.codechef.ffds.databinding.Register2ActivityBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity2 : AppCompatActivity() {

    private var gender:String?="Male"
    lateinit var binding:Register2ActivityBinding
    private val viewModel = ViewModelProvider(this, UserViewModelFactory(application)).get(UserViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Register2ActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            submitBtn.setOnClickListener {
                prompt.visibility = View.GONE
                val phone = phoneNumber.text.toString().trim()
                val name = fullName.text.toString().trim()
                if (phone.length != 10) {
                    prompt.text = "* Enter a valid phone number"
                    prompt.visibility = View.VISIBLE
                } else if (phone.isEmpty() || name.isEmpty()) {
                    prompt.text = "* Fields can't be empty"
                    prompt.visibility = View.VISIBLE
                } else {
                    //registerUser(name, phone)
                    val user = Profile(name = name, phone = phone, gender = gender!!)
                    viewModel.update(user)

                    startActivity(Intent(baseContext, MainActivity::class.java))
                }
            }
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            gender= when (view.getId()) {
                R.id.male -> "Male"
                R.id.female -> "Female"
                R.id.others -> "Others"
                else -> "Rather not say"
            }
        }
    }

    fun registerUser(name:String, phone:String){

        var user = Profile(name = name, phone = phone, gender = gender!!)
        viewModel.update(user)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://ffds-new.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiHolder=retrofit.create(ApiHolder::class.java)

        user = viewModel.getUserData()
        val fields= mutableMapOf(
            "name" to name,
            "gender" to gender,
            "password" to user.password,
            "email" to user.email,
            "phone" to phone)

        Api.retrofitService.register(fields)?.enqueue(object: Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Toast.makeText(applicationContext, response.message(), Toast.LENGTH_SHORT).show()
                if(response.message()=="Created") {
                    startActivity(Intent(baseContext, MainActivity::class.java))
                    finish()
                }
            }
        })

    }

    override fun onBackPressed() {
        startActivity(Intent(this, RegisterActivity1::class.java))
    }
}
