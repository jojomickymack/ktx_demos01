package org.central.gdxmenu.app.pages

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.support.v7.widget.Toolbar
import kotlinx.android.synthetic.main.demos_menu_content.view.*
import org.central.gdxmenu.app.R
import org.central.gdxmenu.app.game.GameActivity


class DemosMenuByType : Fragment() {

    private lateinit var myView: View
    private lateinit var demosMenuByTypeFragment: DemosMenuByType
    private lateinit var itemList: RecyclerView
    private var modelDemosList = mutableListOf("model", "model-tinted", "model-custom-shader", "model-animated")
    private var openglDemosList = mutableListOf("triangle", "depthtest")
    private var physicsDemosList = mutableListOf("gravity", "draggable-mousejoint", "chain")
    private var shadersDemosList = mutableListOf("negative", "grayscale", "sepia", "vignette", "simplex", "twist", "blur", "normals", "lightshafts", "water")

    lateinit var adapter: MainDemosByTypeAdaptor
    var chosenType = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.demos_menu_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments!!.containsKey("CHOSEN_TYPE")) chosenType = arguments!!.getString("CHOSEN_TYPE").toString()

        val toolbar = view.findViewById(R.id.demos_menu_toolbar) as Toolbar
        toolbar.title = chosenType

        myView = view
        demosMenuByTypeFragment = this

        itemList = view.findViewById(R.id.demos_menu_item_list) as RecyclerView
        val demosList = when (chosenType) {
            "models" -> modelDemosList
            "opengl" -> openglDemosList
            "physics" -> physicsDemosList
            "shaders" -> shadersDemosList
            else -> shadersDemosList
        }
        adapter = MainDemosByTypeAdaptor(this, demosList)
        itemList.adapter = adapter
    }
}

class MainDemosByTypeAdaptor(private val parentActivity: DemosMenuByType, var values: MutableList<String>) : RecyclerView.Adapter<MainDemosByTypeAdaptor.ViewHolder>() {

    private val onClickListener = View.OnClickListener { v ->
        val item = v.tag

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