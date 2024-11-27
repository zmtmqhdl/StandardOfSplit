package com.example.standardofsplit.presentation.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.standardofsplit.data.model.ReceiptClass
import com.example.standardofsplit.presentation.ui.component.*
import com.example.standardofsplit.presentation.ui.theme.Typography
import com.example.standardofsplit.presentation.viewModel.ReceiptViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
private fun ReceiptColumnHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            text = "상품명",
            modifier = Modifier.weight(1f),
            style = Typography.receiptColumnHeaderTextStyle,
            textAlign = TextAlign.Left
        )
        Text(
            text = "단가 (수량)",
            modifier = Modifier.weight(1f),
            style = Typography.receiptColumnHeaderTextStyle,
            textAlign = TextAlign.Center
        )
        Text(
            text = "금액",
            modifier = Modifier.weight(1f),
            style = Typography.receiptColumnHeaderTextStyle,
            textAlign = TextAlign.Right
        )
    }
}

@Composable
private fun ReceiptItem(
    onClick: () -> Unit,
    productName: String,
    price: String,
    quantity: String,
) {
    val totalCost = (price.toInt() * quantity.toInt()).toString()
    val formattedPrice = formatNumberWithCommas(price)
    val formattedTotalCost = formatNumberWithCommas(totalCost)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = productName,
                modifier = Modifier.weight(1f),
                style = Typography.receiptItemTextStyle,
                textAlign = TextAlign.Left
            )
            Text(
                text = "$formattedPrice ($quantity)",
                modifier = Modifier.weight(1f),
                style = Typography.receiptItemTextStyle,
                textAlign = TextAlign.Center
            )
            Text(
                text = formattedTotalCost,
                modifier = Modifier.weight(1f),
                style = Typography.receiptItemTextStyle,
                textAlign = TextAlign.Right
            )
        }
    }
}

@Composable
fun ReceiptScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val receiptViewModel: ReceiptViewModel = hiltViewModel()

    val receipts by receiptViewModel.receipts.collectAsState()
    val context = LocalContext.current

    var showReceiptAddDialog by remember { mutableStateOf(false) }
    var showReceiptNameUpdateDialog by remember { mutableStateOf<Int?>(null) }

    val expandedStates = remember { mutableStateListOf<Boolean>() }
    var isToastShowing by remember { mutableStateOf(false) }
    var showReceiptItemAddDialog by remember { mutableStateOf<Int?>(null) }
    var showChangeDialog by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    BackHandler { onBack() }

    if (showReceiptAddDialog) {
        ReceiptAddDialog(
            onDismiss = { showReceiptAddDialog = false },
            onConfirm = { newName ->
                receiptViewModel.receiptAdd(
                    ReceiptClass(
                        placeName = newName,
                        productName = MutableStateFlow(mutableListOf()),
                        productPrice = MutableStateFlow(mutableListOf()),
                        productQuantity = MutableStateFlow(mutableListOf()),
                    )
                )
                showReceiptAddDialog = false
            },
            toastMessage = { message ->
                showCustomToast(context, message)
            }
        )
    }

    showReceiptNameUpdateDialog?.let { index ->
        ReceiptNameUpdateDialog(
            onDismiss = { showReceiptNameUpdateDialog = null },
            onConfirm = { newName ->
                receiptViewModel.receiptNameUpdate(index, newName)
                showReceiptNameUpdateDialog = null
            },
            onDelete = {
                receiptViewModel.receiptDelete(index)
                showReceiptNameUpdateDialog = null
            },
            toastMessage = { message ->
                showCustomToast(context, message)
            },
            name = receipts[index].placeName
        )
    }

//    // 상품 추가 다이얼로그
//    showReceiptItemAddDialog?.let { index ->
//        ProductAddDialog(
//            onDismiss = { showReceiptItemAddDialog = null },
//            onConfirm = { productName, price, quantity ->
//                receiptViewModel.addReceiptItem(
//                    index = index,
//                    productName = productName,
//                    productQuantity = quantity,
//                    productPrice = price
//                )
//                showReceiptItemAddDialog = null
//            },
//            onShowToast = { message ->
//                showCustomToast(context, message)
//            }
//        )
//    }
//
//    // 상품 수정 다이얼로그
//    showChangeDialog?.let { (receiptIndex, itemIndex) ->
//        val productNames by receipts[receiptIndex].productName.collectAsState()
//        val productQuantities by receipts[receiptIndex].productQuantity.collectAsState()
//        val productPrices by receipts[receiptIndex].productPrice.collectAsState()
//
//        Receipt_Change_Dialog(
//            onDismiss = { showChangeDialog = null },
//            onConfirm = { productName, price, quantity ->
//                receiptViewModel.updateReceiptDetail(
//                    index = receiptIndex,
//                    itemIndex = itemIndex,
//                    productName = productName,
//                    productQuantity = quantity,
//                    productPrice = price
//                )
//                showChangeDialog = null
//            },
//            onDelete = {
//                receiptViewModel.deleteReceiptItem(receiptIndex, itemIndex)
//                showChangeDialog = null
//            },
//            productName = productNames[itemIndex],  // 상품명
//            price = productPrices[itemIndex],        // 가격
//            quantity = productQuantities[itemIndex]
//        )
//    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(receipts) { index, receipt ->
                if (expandedStates.size <= index) {
                    expandedStates.add(false)
                }
                Card(
                    modifier = Modifier
                        .width(420.dp)
                        .wrapContentHeight()
                        .padding(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                ) {
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val productPrices by receipt.productPrice.collectAsState()
                            val productQuantities by receipt.productQuantity.collectAsState()
                            val totalCost = formatNumberWithCommas(
                                productPrices.zip(productQuantities) { price, quantity ->
                                    // 안전하게 변환하기
                                    val priceInt = price.toIntOrNull() ?: 0  // 변환 실패 시 0으로 설정
                                    val quantityInt = quantity.toIntOrNull() ?: 0  // 변환 실패 시 0으로 설정
                                    priceInt * quantityInt
                                }.sum().toString()
                            )
                            Text(
                                text = "${receipt.placeName} (${totalCost}원)",
                                modifier = Modifier.clickable { showReceiptNameUpdateDialog = index },
                                style = Typography.receiptHeadTextStyle
                            )
                            ReceiptOpenCloseButton(
                                text1 = "영수증 접기",
                                text2 = "영수증 펼치기",
                                onClick = { expandedStates[index] = !expandedStates[index] },
                                flag = expandedStates[index]
                            )
                        }
                        if (expandedStates[index]) {
                            val productNames by receipt.productName.collectAsState()
                            val productPrices by receipt.productPrice.collectAsState()
                            val productQuantities by receipt.productQuantity.collectAsState()

                            Column {
                                Divider(modifier = Modifier.padding(top = 15.dp))
                                ReceiptColumnHeaders()
                                Divider()
                                productPrices.indices.forEach { i ->
                                    ReceiptItem(
                                        onClick = { showChangeDialog = Pair(index, i) },
                                        productName = productNames[i],
                                        price = productPrices[i],
                                        quantity = productQuantities[i]
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AddButton(
                                        text = "상품 추가",
                                        onClick = { showReceiptItemAddDialog = index }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AddButton(
                        text = "영수증 추가",
                        onClick = { showReceiptAddDialog = true }
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
        ) {
            SubmitButton(
                text = "정산 시작",
                onClick = {
                    // 유효한 영수증을 위한 상품 이름 수집
//                    val hasValidReceipt = receipts.any { receipt ->
//                        receipt.productName.collectAsState().value.isNotEmpty()
//                    }
//                    if (hasValidReceipt) {
//                        onNext()
//                    } else if (!isToastShowing) {
//                        isToastShowing = true
//                        showCustomToast(context, "최소 1개 이상의 상품이 포함된 영수증이 1개 이상 필요합니다.")
//                        MainScope().launch {
//                            delay(2000)
//                            isToastShowing = false
//                        }
//                    }
                }
            )
        }
    }
}