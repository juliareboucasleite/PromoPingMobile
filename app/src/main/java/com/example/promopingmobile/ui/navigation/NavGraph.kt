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
import com.example.promopingmobile.ui.screens.SplashScreen
import com.example.promopingmobile.ui.screens.WelcomeScreen
import com.example.promopingmobile.ui.state.PromoViewModel

object Destinations {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
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
    modifier: Modifier = Modifier
) {
    val authState = viewModel.authState.collectAsState()

    LaunchedEffect(authState.value.isAuthenticated) {
        if (authState.value.isAuthenticated) {
            navController.navigate(Destinations.MAIN_GRAPH) {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val currentRoute = navController.currentDestination?.route
            val inMain = currentRoute in listOf(
                Destinations.MAIN_GRAPH,
                Destinations.DASHBOARD,
                Destinations.PRODUCTS,
                Destinations.PROFILE,
                Destinations.PLANS
            )
            if (inMain) {
                navController.navigate(Destinations.WELCOME) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH,
        modifier = modifier
    ) {
        composable(Destinations.SPLASH) {
            SplashScreen(
                onFinished = {
                    if (authState.value.isAuthenticated) {
                        navController.navigate(Destinations.MAIN_GRAPH) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Destinations.WELCOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Destinations.WELCOME) {
            WelcomeScreen(
                onContinue = {
                    navController.navigate(Destinations.AUTH_GRAPH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
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
