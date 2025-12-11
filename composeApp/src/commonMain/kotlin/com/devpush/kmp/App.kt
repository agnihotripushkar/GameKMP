package com.devpush.kmp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState // Add this
import com.devpush.kmp.navigation.GameNavGraph
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.ui.tooling.preview.Preview

import androidx.navigation.createGraph
import io.github.aakira.napier.Napier
import com.devpush.kmp.ui.theme.GameKMPTheme

@Composable
@Preview
fun App() {
    Napier.v("Hello Napier")
    GameKMPTheme {
        val navHostController = rememberNavController()
        val bottomPadding = WindowInsets.statusBars.asPaddingValues().calculateBottomPadding()

        // Track current route to show/hide bottom bar
        val navBackStackEntry by navHostController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val currentRoute = currentDestination?.route

        // Define Bottom Navigation Items
        val items = listOf(
            NavigationItem(
                label = "Home",
                icon = Icons.Default.Home,
                route = GameNavGraph.Dest.Game
            ),
            NavigationItem(
                label = "Collections",
                icon = Icons.Default.Favorite, // Or List/Collections icon
                route = GameNavGraph.Dest.Collections
            ),
            NavigationItem(
                label = "Statistics",
                icon = Icons.Default.DateRange, // Placeholder for stats
                route = GameNavGraph.Dest.Statistics
            )
        )

        // Show BottomBar only on top-level screens
        val showBottomBar = items.any { 
            // Strict check to ensure we don't show it for screens with "Game" in their package name
            currentRoute == it.route::class.qualifiedName
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        items.forEach { item ->
                            // Determine if selected.
                            // Use strict equality for objects (no params) to avoid ambiguous partial matches
                            // e.g., "Game" in "GameNavGraph"
                            val selected = currentRoute == item.route::class.qualifiedName
                            
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = selected,
                                onClick = {
                                    navHostController.navigate(item.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        // on the back stack as users select items
                                        popUpTo(GameNavGraph.Dest.Game) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            val navGraph = remember(navHostController) {
                navHostController.createGraph(startDestination = GameNavGraph.Dest.Root) {
                    GameNavGraph.build(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        navHostController = navHostController,
                        navGraphBuilder = this
                    )
                }
            }

            NavHost(
                navController = navHostController,
                graph = navGraph,
                modifier = Modifier.padding(innerPadding).fillMaxSize() // Use innerPadding from Scaffold!
            )
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: GameNavGraph.Dest
)