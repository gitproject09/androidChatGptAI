package com.supan.aichatgpt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.supan.aichatgpt.MessageAdapter.MyViewHolder

class MessageAdapter(private var messageList: List<Message>) : RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val chatView = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, null)
        return MyViewHolder(chatView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = messageList[position]
        if (message.sentBy == Message.Companion.SEND_BY_ME) {
            holder.left_chat_view.visibility = View.GONE
            holder.right_chat_view.visibility = View.VISIBLE
            holder.right_chat_text_view.text = message.message
        } else {
            holder.right_chat_view.visibility = View.GONE
            holder.left_chat_view.visibility = View.VISIBLE
            holder.left_chat_text_view.text = message.message
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var left_chat_view: MaterialCardView = itemView.findViewById(R.id.left_chat_view)
        var right_chat_view: MaterialCardView = itemView.findViewById(R.id.right_chat_view)
        var left_chat_text_view: TextView = itemView.findViewById(R.id.left_chat_text_view)
        var right_chat_text_view: TextView = itemView.findViewById(R.id.right_chat_text_view)
    } // MyViewHolder End Here ===================
} // MessageAdapter End Here =====================

