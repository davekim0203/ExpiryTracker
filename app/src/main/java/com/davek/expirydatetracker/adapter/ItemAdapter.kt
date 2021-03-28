package com.davek.expirydatetracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.davek.expirydatetracker.R
import com.davek.expirydatetracker.database.FoodItem
import com.davek.expirydatetracker.databinding.ListItemFoodBinding
import com.davek.expirydatetracker.util.getRemainingDays
import com.davek.expirydatetracker.viewmodel.ItemListViewModel

class ItemAdapter(private val viewModel: ItemListViewModel, private val todayDate: Long) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    var data = listOf<FoodItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun getFoodItemByPosition(position: Int): FoodItem {
        return data[position]
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(viewModel, todayDate, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(
        val binding: ListItemFoodBinding,
        context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        private val mContext = context

        fun bind(viewModel: ItemListViewModel, todayDate: Long, item: FoodItem) {
            val remainingDays = getRemainingDays(item.expiryDate.time, todayDate)
            binding.tvRemainingDays.text = String.format(
                mContext.getString(R.string.remaining_days),
                remainingDays
            )
            binding.statusBar.setBackgroundColor(
                ContextCompat.getColor(
                    mContext,
                    when {
                        remainingDays < 7 -> R.color.status_red
                        remainingDays < 14 -> R.color.status_yellow
                        else -> R.color.status_green
                    }
                )
            )
            binding.itemListVM = viewModel
            binding.food = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemFoodBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, parent.context)
            }
        }
    }
}