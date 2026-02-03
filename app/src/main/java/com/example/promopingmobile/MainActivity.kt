package com.example.promopingmobile

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.promopingmobile.ui.navigation.Destinations
import com.example.promopingmobile.ui.navigation.PromoNavGraph
import com.example.promopingmobile.ui.state.PromoViewModel
import com.example.promopingmobile.ui.state.PromoViewModelFactory
import com.example.promopingmobile.ui.theme.OrangeDark
import com.example.promopingmobile.ui.theme.OrangeLight
import com.example.promopingmobile.ui.theme.OrangePrimary
import com.example.promopingmobile.ui.theme.PromoPingMobileTheme
import com.example.promopingmobile.ui.theme.Sand

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val container = remember { AppContainer(applicationContext) }
            val viewModel: PromoViewModel = viewModel(factory = PromoViewModelFactory(container.repository))
            val navController = rememberNavController()
            val currentBackStack by navController.currentBackStackEntryAsState()
            val currentDestination = currentBackStack?.destination
            val showBottomBar = currentDestination.inMainGraph()
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { }
            val permissions = remember {
                buildList {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.NEARBY_WIFI_DEVICES)
                    }
                }
            }
            val allGranted = permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
            LaunchedEffect(allGranted) {
                if (!allGranted) {
                    permissionLauncher.launch(permissions.toTypedArray())
                }
            }

            PromoPingMobileTheme(darkTheme = false) {
                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            PromoBottomBar(navController)
                        }
                    }
                ) { innerPadding ->
                    PromoNavGraph(
                        viewModel = viewModel,
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private fun NavDestination?.inMainGraph(): Boolean {
    return this?.route in listOf(
        Destinations.DASHBOARD,
        Destinations.PRODUCTS,
        Destinations.PROFILE,
        Destinations.PLANS,
        Destinations.MAIN_GRAPH
    )
}

@Composable
private fun PromoBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Dashboard", Destinations.DASHBOARD, Icons.Default.Dashboard),
        BottomNavItem("Produtos", Destinations.PRODUCTS, Icons.Default.ShoppingCart),
        BottomNavItem("Planos", Destinations.PLANS, Icons.Default.PriceChange),
        BottomNavItem("Perfil", Destinations.PROFILE, Icons.Default.Person)
        
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Sand) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OrangePrimary,
                    selectedTextColor = OrangeDark,
                    indicatorColor = OrangeLight
                )
            )
        }
    }
}

private data class BottomNavItem(val label: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
