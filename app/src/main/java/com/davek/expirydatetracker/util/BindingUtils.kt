package com.davek.expirydatetracker.util

import android.view.View.*
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.davek.expirydatetracker.R
import com.davek.expirydatetracker.adapter.ItemAdapter
import com.davek.expirydatetracker.database.FoodItem
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("expireSoonCount")
fun TextView.setExpireSoonCountText(count: Int?) {
    count?.let {
        text = String.format(resources.getString(R.string.expire_soon_count), it)
    }
}

@BindingAdapter("expiredCount")
fun TextView.setExpiredCountText(count: Int?) {
    count?.let {
        if (it > 0) {
            text = String.format(resources.getString(R.string.expired_count), it)
            visibility = VISIBLE
        } else {
            visibility = GONE
        }
    }
}

@BindingAdapter("foodName")
fun TextView.setFoodNameText(item: FoodItem?) {
    item?.let {
        text = item.name
    }
}

@BindingAdapter("expiryDateWithTitle")
fun TextView.setExpiryDateWithTitleText(item: FoodItem?) {
    item?.let {
        text = String.format(
            resources.getString(R.string.expiry_date_with_title),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(item.expiryDate)
        )
    }
}

@BindingAdapter("expiryDate")
fun TextView.setExpiryDateText(item: Date?) {
    item?.let {
        text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(item)
    }
}

@BindingAdapter("quantity")
fun TextView.setQuantityText(item: FoodItem?) {
    item?.let {
        text = item.quantity.toString()
    }
}

@BindingAdapter("foodItems")
fun setFoodItemList(listView: RecyclerView, items: List<FoodItem>?) {
    items?.let {
        (listView.adapter as ItemAdapter).data = (it)
    }
}