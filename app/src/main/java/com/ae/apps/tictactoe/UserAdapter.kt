package com.ae.apps.tictactoe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.ArrayList

class UserAdapter(context: Context, users: ArrayList<User>) : ArrayAdapter<User>(context, 0, users) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val user = getItem(position) as User

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_list, parent, false)
        }

        val text1 = convertView!!.findViewById<View>(R.id.user_id) as TextView
        val text2 = convertView.findViewById<View>(R.id.name) as TextView
        val text3 = convertView.findViewById<View>(R.id.emailAddress) as TextView

        text1.text = user.myID
        text2.text = user.name
        text3.text = user.email

        return convertView
    }
}
