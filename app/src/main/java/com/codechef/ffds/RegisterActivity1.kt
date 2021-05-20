package com.codechef.ffds

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.codechef.ffds.databinding.Register1ActivityBinding

class RegisterActivity1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = Register1ActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            registerBtn.setOnClickListener {
                val email = emailInput.text.toString()
                val pass = passInput.text.toString()
                val confirm = confirmPass.text.toString()

                if (!email.contains("vitstudent.ac.in")) {
                    prompt.text = "* Enter a valid VIT email"
                    prompt.visibility = View.VISIBLE
                } else if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                    prompt.visibility = View.VISIBLE
                    prompt.text = "* Fields can't be empty"
                } else if (pass != confirm) {
                    prompt.visibility = View.VISIBLE
                    prompt.text = "* Passwords don't match"
                } else {
                    saveUser(email, pass)
                }
            }

            accountExists.setOnClickListener {
                startActivity(
                    Intent(
                        baseContext,
                        LoginActivity::class.java
                    )
                )
            }
        }
    }

    private fun saveUser(email:String, pass:String){
        val tinyDB:TinyDB= TinyDB(baseContext)

        tinyDB.putString("Email", email)
        tinyDB.putString("Password", pass)

        startActivity(Intent(this, RegisterActivity2::class.java))
        finish()
    }
}
