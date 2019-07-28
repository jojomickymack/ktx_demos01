package org.central.gdxmenu.app.pages

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.demos_menu_content.view.*
import org.central.gdxmenu.app.R


class DemosMainMenu : Fragment() {

    private lateinit var myView: View
    private lateinit var demosMenuFragment: DemosMainMenu
    private lateinit var itemList: RecyclerView
    private var demosList = mutableListOf("ktx-actors", "ktx-ashley", "bullet", "models", "opengl", "shaders", "box2d")
    private lateinit var adapter: MainDemosAdaptor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.demos_menu_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myView = view
        demosMenuFragment = this

        itemList = view.findViewById(R.id.demos_menu_item_list) as RecyclerView
        adapter = MainDemosAdaptor(this, demosList)
        itemList.adapter = adapter
    }
}

class MainDemosAdaptor(private val parentActivity: DemosMainMenu, var values: MutableList<String>) : RecyclerView.Adapter<MainDemosAdaptor.ViewHolder>() {

    private val onClickListener = View.OnClickListener { v ->
        val item = v.tag
        val bundle = Bundle()

        bundle.putString("CHOSEN_TYPE", item.toString())

        val menuByType = DemosMenuByType()
        menuByType.arguments = bundle

        parentActivity.fragmentManager!!.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.parent_container, menuByType)
                .addToBackStack("MainDemosMenu")
                .commit()


//        val intent = Intent(v.context, GameActivity::class.java)
//        intent.putExtra("game_choice", item.toString())
//
//        parentActivity.context?.startActivity(intent)
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