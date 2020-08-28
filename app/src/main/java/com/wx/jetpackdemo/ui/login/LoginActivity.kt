package com.wx.jetpackdemo.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tencent.connect.UserInfo
import com.tencent.connect.auth.QQToken
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.wx.jetpackdemo.R
import com.wx.jetpackdemo.model.User
import com.wx.libnetwork.ApiResponse
import com.wx.libnetwork.ApiService
import com.wx.libnetwork.JsonCallback
import kotlinx.android.synthetic.main.activity_layout_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity(), View.OnClickListener {

//    private val tencent: Tencent by lazy {
//        Tencent.createInstance("101794421", applicationContext)
//    }

    private lateinit var tencent : Tencent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_login)
        action_close.setOnClickListener(this)
        action_login.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.action_close -> finish()
            R.id.action_login -> login()
        }
    }

    private fun login() {
        tencent = Tencent.createInstance("101866843", applicationContext)
        tencent.login(this, "all", loginListener)
    }

    private val loginListener = object : IUiListener{
        override fun onComplete(p0: Any?) {
            val response = p0 as JSONObject?
            response?.also {
                val openid = it.getString("openid")
                val access_token = it.getString("access_token")
                val expires_in = it.getString("expires_in")
                val expires_time = it.getString("expires_time")

                tencent.openId = openid
                tencent.setAccessToken(access_token, expires_in)
                getUserInfo(tencent.qqToken, expires_time, openid)
            }
        }

        override fun onCancel() {
            Toast.makeText(applicationContext, "登录取消", Toast.LENGTH_SHORT).show()
        }

        override fun onError(p0: UiError?) {
            Toast.makeText(applicationContext, "登录失败: ${p0.toString()}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserInfo(qqToken: QQToken?, expiresTime: String, openid: String) {
        val userInfo = UserInfo(applicationContext, qqToken)
        userInfo.getUserInfo(object : IUiListener{
            override fun onComplete(p0: Any?) {
                val response = p0 as JSONObject?
                response?.also {
                    val nickname = it.getString("nickname")
                    val figureurl_2 = it.getString("figureurl_2")
                    save(nickname, figureurl_2, openid, expiresTime)
                }
            }

            override fun onCancel() {
                Toast.makeText(applicationContext, "登录取消", Toast.LENGTH_SHORT).show()
            }

            override fun onError(p0: UiError?) {
                Toast.makeText(applicationContext, "登录失败: ${p0.toString()}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // /user/insert
    private fun save(nickname: String, avatar: String, openid: String, expires_time: String) {
        ApiService.get<User>("/user/insert")
            .addParam("name", nickname)
            .addParam("avatar", avatar)
            .addParam("qqOpenId", openid)
            .addParam("expires_time", expires_time)
            .execute(object : JsonCallback<User>(){
                override fun onSuccess(response: ApiResponse<User>) {
                    response.body?.let { UserManager.save(it) }
                    finish()
                }

                override fun onError(response: ApiResponse<User>) {
                    Toast.makeText(applicationContext, "登陆失败,msg: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}