package org.central.gdxmenu.app.pages

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.demos_menu_content.view.*
import org.central.gdxmenu.app.R
import org.central.gdxmenu.app.game.GameActivity


class DemosMenuRecyclerViewAdapter(private val parentActivity: DemosMenu, var values: MutableList<String>) : RecyclerView.Adapter<DemosMenuRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener = View.OnClickListener { v ->
        val item = v.tag
        val bundle = Bundle()
        val intent = Intent(v.context, GameActivity::class.java)
        intent.putExtra("game_choice", item.toString())

        parentActivity.context?.startActivity(intent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.demos_menu_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount() = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView = view.id_text
    }
}