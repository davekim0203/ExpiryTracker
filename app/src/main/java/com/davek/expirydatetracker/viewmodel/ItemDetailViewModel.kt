package com.davek.expirydatetracker.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.davek.expirydatetracker.database.DataResult
import com.davek.expirydatetracker.database.FoodItem
import com.davek.expirydatetracker.database.FoodItemRepository
import com.davek.expirydatetracker.receiver.AlarmReceiver
import com.davek.expirydatetracker.util.SingleLiveEvent
import com.davek.expirydatetracker.util.cancelAlarm
import com.davek.expirydatetracker.util.getCalendarInstance
import com.davek.expirydatetracker.util.getRemainingDays
import com.davek.expirydatetracker.viewmodel.ItemListViewModel.Companion.DEFAULT_ITEM_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val foodItemRepository: FoodItemRepository
) : ViewModel() {

    private val weakContext: WeakReference<Context> = WeakReference(context)

    private var foodItemId: Long = DEFAULT_ITEM_ID

    private val _showSnackbar = SingleLiveEvent<Int>()
    val showSnackbar: LiveData<Int>
        get() = _showSnackbar

    private val _showDatePicker = SingleLiveEvent<Date>()
    val showDatePicker: LiveData<Date>
        get() = _showDatePicker

    private val _navigateToItemList = SingleLiveEvent<Any>()
    val navigateToItemList: LiveData<Any>
        get() = _navigateToItemList

    /**
     * Exposing MutableLiveData for two-way data binding
     * Should be only set from xml or test
     */
    val foodName = MutableLiveData<String>()
    val note = MutableLiveData<String>()

    private val _expiryDate = MutableLiveData<Date>()
    val expiryDate: LiveData<Date>
        get() = _expiryDate

    private val _quantity = MutableLiveData<Int>()
    val quantity: LiveData<Int>
        get() = _quantity

    private val _isNotificationOn = MutableLiveData<Boolean>()
    val isNotificationOn: LiveData<Boolean>
        get() = _isNotificationOn

    private var mPrevIsNotificationOn: Boolean? = null
    private var mPrevExpiryDate: Date? = null
    private val selectedDateCalendar: Calendar = getCalendarInstance()
    private var isNewFoodItem: Boolean = false
    private var isItemLoaded = false

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun loadItem(id: Long) {
        if (isItemLoaded) return
        if (id == DEFAULT_ITEM_ID) {
            _isNotificationOn.value = DEFAULT_NOTIFICATION_STATUS
            _expiryDate.value = selectedDateCalendar.time
            isNewFoodItem = true
            _quantity.value = DEFAULT_QUANTITY
            return
        }

        foodItemId = id

        viewModelScope.launch {
            foodItemRepository.getFoodItemById(id).let {
                if (it is DataResult.Success) {
                    onFoodItemLoaded(it.data)
                } else {
                    Log.e("TAG", "Data loading is not successful")
                }
            }
        }
    }

    private fun onFoodItemLoaded(foodItem: FoodItem) {
        foodName.value = foodItem.name
        selectedDateCalendar.time = foodItem.expiryDate
        _expiryDate.value = foodItem.expiryDate
        _quantity.value = foodItem.quantity
        _isNotificationOn.value = foodItem.isNotificationOn
        note.value = foodItem.note
        mPrevIsNotificationOn = foodItem.isNotificationOn
        mPrevExpiryDate = foodItem.expiryDate
        isItemLoaded = true
    }

    fun onQuantityIncreaseButtonClick() {
        _quantity.value?.let {
            if (it < 100) {
                _quantity.value = it + 1
            }
        }
    }

    fun onQuantityDecreaseButtonClick() {
        _quantity.value?.let {
            if (it > 0) {
                _quantity.value = it - 1
            }
        }
    }

    fun onSaveButtonClick() {
        val currentFoodName = foodName.value
        val currentIsNotificationOn = _isNotificationOn.value ?: true
        val currentQuantity = _quantity.value ?: DEFAULT_QUANTITY

        if (currentFoodName == null || currentFoodName == "") {
            _showSnackbar.value = SNACKBAR_ID_NAME_REQUIRED
            return
        }

        val itemToSave = FoodItem(
            name = currentFoodName,
            expiryDate = selectedDateCalendar.time,
            quantity = currentQuantity,
            isNotificationOn = currentIsNotificationOn,
            note = note.value
        )

        if (isNewFoodItem) {
            insertFoodItem(itemToSave, currentFoodName, currentIsNotificationOn)
        } else {
            itemToSave.itemId = foodItemId
            updateFoodItem(itemToSave)
            updateNotification(
                currentIsNotificationOn,
                selectedDateCalendar.time,
                itemToSave.itemId.toInt(),
                currentFoodName
            )
        }

        _navigateToItemList.call()
    }

    fun onDateClick() {
        _showDatePicker.value = selectedDateCalendar.time
    }

    fun onDateSelected(year: Int, month: Int, day: Int) {
        selectedDateCalendar.set(year, month, day)
        _expiryDate.value = selectedDateCalendar.time
    }

    fun onNotificationToggleChanged(isChecked: Boolean) {
        _isNotificationOn.value = isChecked
    }

    private fun setNotification(foodId: Int, foodName: String) {
        weakContext.get()?.let {
            val notifyIntent = Intent(it, AlarmReceiver::class.java)
            notifyIntent.putExtra("foodId", foodId)
            notifyIntent.putExtra("foodName", foodName)
            notifyIntent.putExtra("expiryDate", selectedDateCalendar.timeInMillis)
            val notifyPendingIntent = PendingIntent.getBroadcast(
                it,
                foodId,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val remainingDays =
                getRemainingDays(selectedDateCalendar.time.time, getCalendarInstance().time.time)
            when {
                remainingDays > 7 -> {
                    val time =
                        selectedDateCalendar.timeInMillis - (DAY_IN_MILLI * 7) + TEN_HOUR_IN_MILLI
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        AlarmManager.INTERVAL_DAY,
                        notifyPendingIntent
                    )
                }
                remainingDays > 0 -> {
                    val time =
                        selectedDateCalendar.timeInMillis - (DAY_IN_MILLI * (remainingDays - 1)) + TEN_HOUR_IN_MILLI
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        AlarmManager.INTERVAL_DAY,
                        notifyPendingIntent
                    )
                }
            }
        }
    }

    private fun updateNotification(
        currentIsNotificationOn: Boolean,
        currentExpiryDate: Date,
        foodId: Int,
        foodName: String
    ) {
        val prevIsNotificationOn = mPrevIsNotificationOn
        val prevExpiryDate = mPrevExpiryDate

        if (prevIsNotificationOn == null || prevExpiryDate == null) {
            _showSnackbar.value = SNACKBAR_ID_NOTIFICATION_ERROR
            Log.e(
                "TAG",
                "Should not be null: prevIsNotificationOn=$prevIsNotificationOn, prevExpiryDate=$prevExpiryDate"
            )
            return
        }

        if (currentIsNotificationOn) {
            if (!prevIsNotificationOn || currentExpiryDate != prevExpiryDate) {
                setNotification(foodId, foodName)
            }
        } else if (prevIsNotificationOn) {
            weakContext.get()?.let {
                cancelAlarm(it, foodId)
            }
        }
    }

    private fun insertFoodItem(foodItem: FoodItem, foodName: String, isNotificationOn: Boolean) =
        viewModelScope.launch {
            foodItemRepository.insertFoodItem(foodItem).let {
                if (it is DataResult.Success) {
                    if (isNotificationOn) {
                        setNotification(it.data.toInt(), foodName)
                    }
                } else {
                    Log.e("TAG", "Failed to insert item")
                }
            }
        }

    private fun updateFoodItem(foodItem: FoodItem) = viewModelScope.launch {
        foodItemRepository.updateFoodItem(foodItem)
    }

    fun deleteFoodItem(context: Context) = viewModelScope.launch {
        if (foodItemId != DEFAULT_ITEM_ID) {
            foodItemRepository.deleteFoodItemById(foodItemId)
            cancelAlarm(context, foodItemId.toInt())
            _navigateToItemList.call()
            _showSnackbar.value = SNACKBAR_ID_ITEM_DELETED
        }
    }

    companion object {
        const val DEFAULT_NOTIFICATION_STATUS = true
        const val DEFAULT_QUANTITY: Int = 1

        const val DAY_IN_MILLI: Long = 24 * 60 * 60 * 1000
        private const val TEN_HOUR_IN_MILLI: Long = 10 * 60 * 60 * 1000

        const val SNACKBAR_ID_ITEM_DELETED: Int = 0
        const val SNACKBAR_ID_NAME_REQUIRED: Int = 1
        const val SNACKBAR_ID_NOTIFICATION_ERROR: Int = 2
    }
}