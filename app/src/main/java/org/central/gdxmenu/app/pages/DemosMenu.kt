package org.central.gdxmenu.app.pages

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.AlertDialog
import android.app.Dialog
import org.central.gdxmenu.app.R


class DemosMenu : Fragment() {

    lateinit var myView: View
    lateinit var demosMenuFragment: DemosMenu
    lateinit var itemList: RecyclerView
    var demosList = mutableListOf("negative", "stencil", "sepia", "simplex", "blur", "normals", "models", "lightshafts", "water", "noonoo")
    lateinit var adapter: DemosMenuRecyclerViewAdapter

    private lateinit var dialog: Dialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.demos_menu_main, container, false)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setView(R.layout.spinner)
        dialog = builder.create()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myView = view
        demosMenuFragment = this

        itemList = view.findViewById(R.id.search_results_item_list) as RecyclerView
        adapter = DemosMenuRecyclerViewAdapter(this, demosList)
        itemList.adapter = adapter
    }
}