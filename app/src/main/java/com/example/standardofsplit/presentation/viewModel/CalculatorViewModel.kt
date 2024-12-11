package com.example.standardofsplit.presentation.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.standardofsplit.data.model.ReceiptClass
import com.example.standardofsplit.data.model.TotalPay
import com.example.standardofsplit.presentation.ui.component.showCustomToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    startViewModel: StartViewModel,
    receiptViewModel: ReceiptViewModel
) : ViewModel() {

    private val personCount: StateFlow<Int> = startViewModel.personCount

    private val receipts: StateFlow<List<ReceiptClass>> = receiptViewModel.receipts

    private val _totalPay = MutableStateFlow(TotalPay())

    private val _stack = MutableStateFlow<MutableList<Any>>(mutableListOf())

    private val _buttonNames = MutableStateFlow<MutableList<String>>(mutableListOf())
    val buttonNames: StateFlow<MutableList<String>> = _buttonNames

    private val _buttonStates = MutableStateFlow(List(8) { false })
    val buttonStates: StateFlow<List<Boolean>> = _buttonStates

    private val _buttonPermissions = MutableStateFlow(List(8) { false })

    private val _receiptKey = MutableStateFlow(0)
    val receiptKey: StateFlow<Int> = _receiptKey

    private val _productKey = MutableStateFlow(0)
    val productKey: StateFlow<Int> = _productKey

    private val _changeMode = MutableStateFlow(false)
    val changeMode: StateFlow<Boolean> = _changeMode

    private val _showButtonNameChangeDialog = MutableStateFlow(false)

    fun setChangeMode() {
        _changeMode.value = !_changeMode.value
    }

    private fun buttonPush(index: Int) {
        _buttonStates.value = _buttonStates.value.toMutableList().apply { this[index] = !this[index] }
    }

    private fun resetButtonStates() {
        _buttonStates.value = List(8) { false }
    }

    private fun trueButtonStates() {
        _buttonStates.value = List(8) { true }
    }

    fun initializeTotalPay() {
        val initialTotalPay =
            (0..7).associateWith { mutableMapOf<String, MutableMap<String, Int>>() }
        _totalPay.value.payment.value = initialTotalPay.toMutableMap()
        setReceiptKey(value = 0)
        setProductKey(value = 0)
    }

    fun updateTotalPay(
        payList: List<Int>,
        placeName: String,
        productName: String,
        productPrice: Int,
    ) {
        try {
            val dividedPrice: Int =
                (kotlin.math.ceil((productPrice.toDouble() / payList.size) / 10) * 10).toInt()
            val current = _totalPay.value.payment.value

            for (i in payList) {
                val updatedProducts = current[i]?.get(placeName) ?: mutableMapOf()
                updatedProducts[productName] = dividedPrice
                current[i] =
                    current[i]?.apply { this[placeName] = updatedProducts } ?: mutableMapOf(
                        placeName to updatedProducts
                    )
            }
            _totalPay.value.payment.value = current
        } catch (_: Exception) {
        }
    }

    fun setReceiptKey(value: Int) {
        _receiptKey.value = value
    }

    fun incrementReceiptKey() {
        _receiptKey.value += 1
    }

    fun decrementReceiptKey() {
        _receiptKey.value -= 1
    }

    fun setProductKey(value: Int) {
        _productKey.value = value
    }

    fun incrementProductKey() {
        _productKey.value += 1
    }

    fun decrementProductKey() {
        _productKey.value -= 1
    }

    fun openButtonNameChangeDialog() {
        _showButtonNameChangeDialog.value = true
    }

    fun closeButtonNameChangeDialog() {
        _showButtonNameChangeDialog.value = false
    }

    fun initializeButtonNames() {
        _buttonNames.value.clear()
        for (i in 0..7) {
            if (i < personCount.value) {
                _buttonNames.value.add("X")
                _buttonPermissions.value =
                    _buttonPermissions.value.toMutableList().apply { this[i] = true }
            } else {
                _buttonNames.value.add("인원$i")
            }
        }
    }

    fun updateButtonNames(index: Int, newName: String) {
        _buttonNames.value = _buttonNames.value.toMutableList().apply { this[index] = newName }
    }

    fun rollback() {
        val currentStack = _stack.value
        if (currentStack.isNotEmpty()) {
            val lastElement = currentStack.removeAt(currentStack.size - 1)
            _totalPay.value = lastElement as TotalPay
            _stack.value = currentStack
            if (_productKey.value == 0) {
                if (_receiptKey.value > 0) {
                    decrementReceiptKey()
                    setProductKey(lastElement.payment.value.size - 1)
                }
            } else {
                decrementProductKey()
            }
        }
    }

    private fun lastCheck(): Boolean {
        val receiptCount = receipts.value.size
        val productCount = receipts.value[receiptCount - 1].productName.value.size
        return (receiptCount - 1 == _receiptKey.value && productCount - 1 == _productKey.value)
    }

    fun personSelect(index: Int, context: Context) {
        if (_changeMode.value && _buttonPermissions.value[index]) {
            _showButtonNameChangeDialog.value = true
        } else if (!_changeMode.value && lastCheck()) {
            showCustomToast(message = "정산이 완료되었습니다. 정산을 확인해주세요.", context = context)
        } else if (!_changeMode.value && !lastCheck()) {
            buttonPush(index)
        }
    }

    fun endCheck(
        context: Context,
    ) {
        if (lastCheck()) {
            showCustomToast(message = "정산이 완료되었습니다. 정산을 확인해주세요.", context = context)
        } else {
            trueButtonStates()
        }
    }

    fun calculate(
        onNext: () -> Unit,
        context: Context
    ) {
        if (lastCheck()) {
            onNext()
        } else {
            if (_buttonStates.value == List(8) { false }) {
                updateTotalPay(
                    payList = _buttonStates.value.mapIndexedNotNull { index, value ->
                        if (value) index else null
                    },
                    placeName = receipts.value[receiptKey.value].placeName,
                    productName = receipts.value[receiptKey.value].productName.value[productKey.value],
                    productPrice =  receipts.value[receiptKey.value].productPrice.value[productKey.value],
                )
                resetButtonStates()
                if (lastCheck()) {
                    showCustomToast(message = "정산이 완료되었습니다. 정산을 확인해주세요.", context = context)
                } else {
                    incrementProductKey()
                    if (productKey.value == receipts.value[receiptKey.value].productPrice.value.size) {
                        setProductKey(0)
                        incrementReceiptKey()
                    }
                }
            } else {
                showCustomToast(message = "정산이 완료되었습니다. 정산을 확인해주세요.", context = context)
            }
        }
    }
}