package com.supan.aichatgpt

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request

import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageTextText: EditText
    private lateinit var sendBtn: ImageView
    private val messageList: MutableList<Message> = ArrayList()
    private lateinit var messageAdapter: MessageAdapter
    private var client: OkHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //====================================
        messageTextText = findViewById(R.id.message_text_text)
        sendBtn = findViewById(R.id.send_btn)
        recyclerView = findViewById(R.id.recyclerView)

        // Create Layout behaves and set it in recyclerView
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter

        if (!isConnected(this@MainActivity)) {
            buildDialog(this@MainActivity).show()
        }

        messageTextText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim { it <= ' ' }.isEmpty()) {
                    sendBtn.isEnabled = false
                    // Toast.makeText(MainActivity.this, "Type your message", Toast.LENGTH_SHORT).show();
                } else {
                    sendBtn.isEnabled = true
                    sendBtn.setOnClickListener { view: View? ->
                        val question = messageTextText.text.toString().trim { it <= ' ' }
                        addToChat(question, Message.SEND_BY_ME)
                        messageTextText.setText("")
                        callAPI(question)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }
        })
    } // OnCreate Method End Here ================

    @SuppressLint("NotifyDataSetChanged")
    fun addToChat(message: String?, sendBy: String) {
        runOnUiThread {
            messageList.add(Message(message, sendBy))
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messageAdapter.itemCount)
        }
    } // addToChat End Here =====================

    fun addResponse(response: String?) {
        messageList.removeAt(messageList.size - 1)
        addToChat(response, Message.SEND_BY_BOT)
    } // addResponse End Here =======

    fun callAPI(question: String?) {
        // okhttp
        messageList.add(Message("Typing...", Message.SEND_BY_BOT))

        val jsonBody = JSONObject()
        try {
            //jsonBody.put("model", "text-davinci-003")
            jsonBody.put("model", "gpt-4o-mini")
            jsonBody.put("prompt", question)
            jsonBody.put("max_tokens", 4000)
            jsonBody.put("temperature", 0)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }

        val requestBody: RequestBody =
            jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request: Request = Request.Builder()
            .url(API.API_URL)
            .header("Authorization", "Bearer ${API.API_KEY}")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("""Failed to load response due to: ${e.message}""".trimIndent())
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonObject: JSONObject
                    try {
                        checkNotNull(response.body)
                        jsonObject = JSONObject(response.body!!.string())
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val result = jsonArray.getJSONObject(0).getString("text")
                        addResponse(result.trim { it <= ' ' })
                    } catch (e: JSONException) {
                        throw RuntimeException(e)
                    }
                } else {
                    checkNotNull(response.body)
                    addResponse("Failed to load response due to: \n" + response.body)
                }
            }
        })
    } // callAPI End Here =============

    private fun isConnected(context: Context): Boolean {
        val manager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = manager.activeNetworkInfo
        if (info != null && info.isConnectedOrConnecting) {
            val wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            val mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            return (mobile != null && mobile.isConnectedOrConnecting) || (wifi != null && wifi.isConnectedOrConnecting)
        } else return false
    }

    private fun buildDialog(context: Context?): AlertDialog.Builder {
        val builder = AlertDialog.Builder(
            context!!
        )
        builder.setTitle("No Internet Connection")
        builder.setMessage("Please check your internet connection.")
        builder.setPositiveButton("OK") { dialog: DialogInterface?, which: Int -> finishAffinity() }
        return builder
    }

    companion object {
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    }
} // Public Class End Here =========================
