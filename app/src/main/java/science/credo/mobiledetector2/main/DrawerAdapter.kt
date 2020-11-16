package science.credo.mobiledetector2.main

import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import science.credo.mobiledetector2.R


class DrawerAdapter(
    private val context: Context,
    private val menu: Menu,
    private val onItemClick: OnItemClick,
    private var avatarUrl: String,
    private var userName: String
) : RecyclerView.Adapter<DrawerAdapter.ViewHolder>() {

    companion object {
        const val ITEM_HEADER = 0
        const val ITEM_MENU_BUTTON = 1
        const val ITEM_FOOTER = 2
    }

    interface OnItemClick{
        fun onDrawerItemClick(menuItem: MenuItem)
    }

    val inflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_HEADER
//            (itemCount - 1) -> ITEM_FOOTER
            else -> ITEM_MENU_BUTTON
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = when (viewType) {
            ITEM_HEADER -> inflater.inflate(R.layout.item_drawer_header, parent, false)
            ITEM_MENU_BUTTON -> inflater.inflate(R.layout.item_drawer_menu_button, parent, false)
//            else -> {
//                val temp = inflater.inflate(R.layout.item_drawer_footer, parent, false)
//                temp.layoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    UiUtils.getScreenHeight() -
//                            UiUtils.dpToPx(160) -
//                            (itemCount - 2) * UiUtils.dpToPx(60)
//                )
//                temp
//            }
            else -> {
                inflater.inflate(R.layout.item_drawer_menu_button, parent, false)
            }
        }
        return ViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return menu.size() + 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM_HEADER -> {
//                holder.tvUsername?.setTypeface(Typeface.PRATA_REGULAR)
//                holder.tvUsername?.text =
//                    String.format(context.getString(R.string.drawer_header_user_name), userName)
//                Picasso.get()
//                    .load(avatarUrl)
//                    .placeholder(R.drawable.ic_photo_thumb)
////                    .into(holder.ivAvatar)
            }
            ITEM_MENU_BUTTON -> {
                val menuItem = menu.getItem(position-1)
                holder.ivMenuItemIcon?.setImageDrawable(menuItem.icon)
                holder.tvMenuItemText?.text = menuItem.title
                holder.itemView.setOnClickListener {
                    onItemClick.onDrawerItemClick(menuItem)
                }
            }
            ITEM_FOOTER -> {

            }
        }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        //header
//        val ivAvatar = view.findViewById<ImageView?>(R.id.ivAvatar)
//        val tvUsername = view.findViewById<TextView?>(R.id.tvUsername)

        //item
        val tvMenuItemText = view.findViewById<TextView?>(R.id.tvMenuItemText)
        val ivMenuItemIcon = view.findViewById<ImageView?>(R.id.ivMenuItemIcon)

        //footer

    }

}