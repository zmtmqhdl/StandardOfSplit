package com.example.standardofsplit.presentation.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.standardofsplit.presentation.ui.navigator.Navigator
import com.example.standardofsplit.presentation.ui.screen.*
import com.example.standardofsplit.presentation.ui.theme.StandardOfSplitTheme
import com.example.standardofsplit.presentation.viewModel.ReceiptViewModel
import com.example.standardofsplit.presentation.viewModel.StartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()
        setContent {
            StandardOfSplitTheme {
                Navigation()
            }
        }
    }
    private fun setupWindow() {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }
}

@Composable
private fun Navigation() {
    val navController = rememberNavController()
    val navigator = remember { Navigator(navController) }

    val startViewModel: StartViewModel = hiltViewModel()
    val receiptViewModel: ReceiptViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Start.route) {
        composable(Screen.Start.route) {
            StartScreen(
                startViewModel = startViewModel,
                onNext = { navigator.navigateToReceipt() },
            )
        }
        composable(Screen.Receipt.route) {
            ReceiptScreen(
                receiptViewModel = receiptViewModel,
                onNext = { navigator.navigateToCalculator() },
                onBack = { navigator.navigateToStart() }
            )
        }
        composable(Screen.Calculator.route) {
            CalculatorScreen(
                startViewModel = startViewModel,
                receiptViewModel = receiptViewModel,
                onNext = { navigator.navigateToResult() },
                onBack = { navigator.navigateToReceipt() }
            )
        }
        composable(Screen.Result.route) {
            ResultScreen(
                onBack = { navigator.navigateToCalculator() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object Start : Screen("StartScreen")
    data object Receipt : Screen("ReceiptScreen")
    data object Calculator : Screen("CalculatorScreen")
    data object Result : Screen("ResultScreen")
}