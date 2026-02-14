package com.animegen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.animegen.app.ui.screen.community.CommunityDetailRoute
import com.animegen.app.ui.screen.community.CommunityFeedRoute
import com.animegen.app.ui.screen.community.CommunityPublishRoute
import com.animegen.app.ui.screen.community.MyFavoritesRoute
import com.animegen.app.ui.screen.community.MyPublishedRoute
import com.animegen.app.ui.screen.create.CreateRoute
import com.animegen.app.ui.screen.settings.SettingsRoute
import com.animegen.app.ui.screen.task.TaskRoute
import com.animegen.app.ui.screen.workdetail.WorkDetailRoute
import com.animegen.app.ui.screen.works.WorksRoute
import com.animegen.app.ui.theme.AnimeGenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimeGenTheme {
                AppNav((application as AnimeGenApp).container)
            }
        }
    }
}

data class BottomItem(val route: String, val title: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppNav(container: AppContainer) {
    val navController = rememberNavController()
    val items = listOf(
        BottomItem("create", "Create"),
        BottomItem("task", "Task"),
        BottomItem("works", "Works"),
        BottomItem("community/feed", "Community"),
        BottomItem("settings", "Settings")
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.route?.startsWith(item.route) == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {},
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "create",
            modifier = Modifier.padding(padding)
        ) {
            composable("create") {
                CreateRoute(container = container) { taskId, workId ->
                    navController.navigate("task/$taskId/$workId")
                }
            }
            composable("task") {
                TaskRoute(
                    container = container,
                    taskIdArg = null,
                    workIdArg = null,
                    onNavigateDetail = { workId -> navController.navigate("workDetail/$workId") }
                )
            }
            composable("task/{taskId}/{workId}") { backStackEntry ->
                TaskRoute(
                    container = container,
                    taskIdArg = backStackEntry.arguments?.getString("taskId"),
                    workIdArg = backStackEntry.arguments?.getString("workId"),
                    onNavigateDetail = { workId -> navController.navigate("workDetail/$workId") }
                )
            }
            composable("works") {
                WorksRoute(container = container) { workId ->
                    navController.navigate("workDetail/$workId")
                }
            }
            composable("workDetail/{workId}") { backStackEntry ->
                WorkDetailRoute(
                    container = container,
                    workIdArg = backStackEntry.arguments?.getString("workId"),
                    onPublish = { workId -> navController.navigate("community/publish/$workId") }
                )
            }
            composable("community/feed") {
                CommunityFeedRoute(
                    container = container,
                    onOpenDetail = { contentId -> navController.navigate("community/detail/$contentId") },
                    onOpenFavorites = { navController.navigate("community/myFavorites") },
                    onOpenPublished = { navController.navigate("community/myPublished") }
                )
            }
            composable("community/detail/{contentId}") { backStackEntry ->
                CommunityDetailRoute(
                    container = container,
                    contentIdArg = backStackEntry.arguments?.getString("contentId")
                )
            }
            composable("community/publish/{workId}") { backStackEntry ->
                CommunityPublishRoute(
                    container = container,
                    workIdArg = backStackEntry.arguments?.getString("workId"),
                    onDone = { contentId -> navController.navigate("community/detail/$contentId") }
                )
            }
            composable("community/myFavorites") {
                MyFavoritesRoute(container = container) { contentId ->
                    navController.navigate("community/detail/$contentId")
                }
            }
            composable("community/myPublished") {
                MyPublishedRoute(container = container) { contentId ->
                    navController.navigate("community/detail/$contentId")
                }
            }
            composable("settings") {
                SettingsRoute(container = container)
            }
        }
    }
}

