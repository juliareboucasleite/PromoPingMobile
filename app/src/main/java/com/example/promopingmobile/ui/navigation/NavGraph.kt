package com.example.promopingmobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.promopingmobile.ui.screens.DashboardScreen
import com.example.promopingmobile.ui.screens.LoginScreen
import com.example.promopingmobile.ui.screens.PlansScreen
import com.example.promopingmobile.ui.screens.ProductsScreen
import com.example.promopingmobile.ui.screens.ProfileScreen
import com.example.promopingmobile.ui.screens.RegisterScreen
import com.example.promopingmobile.ui.state.PromoViewModel

object Destinations {
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val PROFILE = "profile"
    const val PLANS = "plans"
}

@Composable
fun PromoNavGraph(
    viewModel: PromoViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(viewModel.authState.collectAsState().value.isAuthenticated) {
        val authenticated = viewModel.authState.value.isAuthenticated
        if (authenticated) {
            navController.navigate(Destinations.MAIN_GRAPH) {
                popUpTo(0) { inclusive = true }
            }
        } else {
            navController.navigate(Destinations.AUTH_GRAPH) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        navigation(startDestination = Destinations.LOGIN, route = Destinations.AUTH_GRAPH) {
            composable(Destinations.LOGIN) {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { navController.navigate(Destinations.REGISTER) },
                    onLoggedIn = {
                        navController.navigate(Destinations.MAIN_GRAPH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Destinations.REGISTER) {
                RegisterScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegistered = {
                        navController.navigate(Destinations.MAIN_GRAPH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
        navigation(startDestination = Destinations.DASHBOARD, route = Destinations.MAIN_GRAPH) {
            composable(Destinations.DASHBOARD) {
                DashboardScreen(
                    viewModel = viewModel,
                    onOpenProducts = { navController.navigate(Destinations.PRODUCTS) }
                )
            }
            composable(Destinations.PRODUCTS) {
                ProductsScreen(viewModel = viewModel)
            }
            composable(Destinations.PROFILE) {
                ProfileScreen(viewModel = viewModel)
            }
            composable(Destinations.PLANS) {
                PlansScreen(viewModel = viewModel)
            }
        }
    }
}
