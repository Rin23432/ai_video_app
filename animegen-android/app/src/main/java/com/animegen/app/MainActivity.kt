package com.animegen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.animegen.app.ui.screen.auth.LoginRoute
import com.animegen.app.ui.screen.community.CommunityDetailRoute
import com.animegen.app.ui.screen.community.CommunityFeedRoute
import com.animegen.app.ui.screen.community.CommunityPublishRoute
import com.animegen.app.ui.screen.community.MyFavoritesRoute
import com.animegen.app.ui.screen.community.MyPublishedRoute
import com.animegen.app.ui.screen.community.TagDetailRoute
import com.animegen.app.ui.screen.create.CreateRoute
import com.animegen.app.ui.screen.me.MeRoute
import com.animegen.app.ui.screen.me.ProfileRoute
import com.animegen.app.ui.screen.settings.SettingsRoute
import com.animegen.app.ui.screen.task.TaskRoute
import com.animegen.app.ui.screen.workdetail.WorkDetailRoute
import com.animegen.app.ui.screen.works.WorksRoute
import com.animegen.app.ui.theme.AnimeGenTheme
import kotlinx.coroutines.flow.collectLatest

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

data class BottomItem(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppNav(container: AppContainer) {
    val navController = rememberNavController()
    val items = listOf(
        BottomItem("create", R.string.bottom_create, Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome),
        BottomItem("task", R.string.bottom_task, Icons.Outlined.TaskAlt, Icons.Filled.TaskAlt),
        BottomItem("works", R.string.bottom_works, Icons.Outlined.VideoLibrary, Icons.Filled.VideoLibrary),
        BottomItem("community/feed", R.string.bottom_community, Icons.Outlined.Groups, Icons.Filled.Groups),
        BottomItem("me", R.string.bottom_me, Icons.Outlined.Settings, Icons.Filled.Settings)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val swipeThresholdPx = 80f
    val navigateToTab = remember(navController) {
        { route: String ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    LaunchedEffect(Unit) {
        container.loginRequiredFlow.collectLatest {
            if (navController.currentDestination?.route != "login") {
                navController.navigate("login")
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentDestination?.route != "login") {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    items.forEach { item ->
                        val selected = currentDestination?.route?.startsWith(item.route) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navigateToTab(item.route)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.icon,
                                    contentDescription = stringResource(item.titleRes)
                                )
                            },
                            label = { Text(stringResource(item.titleRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "create",
            modifier = Modifier
                .padding(padding)
                .pointerInput(currentRoute) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            totalDrag += dragAmount
                        },
                        onDragEnd = {
                            if (currentRoute == "login" || kotlin.math.abs(totalDrag) < swipeThresholdPx) {
                                totalDrag = 0f
                                return@detectHorizontalDragGestures
                            }
                            val currentIndex = items.indexOfFirst { currentRoute?.startsWith(it.route) == true }
                            if (currentIndex == -1) {
                                totalDrag = 0f
                                return@detectHorizontalDragGestures
                            }
                            val targetIndex = if (totalDrag < 0) currentIndex + 1 else currentIndex - 1
                            val targetRoute = items.getOrNull(targetIndex)?.route
                            if (targetRoute != null) {
                                navigateToTab(targetRoute)
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = { totalDrag = 0f }
                    )
                }
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
                    onOpenPublished = { navController.navigate("community/myPublished") },
                    onOpenTagDetail = { tagId -> navController.navigate("community/tag/$tagId") }
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
            composable("community/tag/{tagId}") { backStackEntry ->
                TagDetailRoute(
                    container = container,
                    tagIdArg = backStackEntry.arguments?.getString("tagId"),
                    onOpenContent = { contentId -> navController.navigate("community/detail/$contentId") }
                )
            }
            composable("me") {
                MeRoute(
                    container = container,
                    onOpenFavorites = { navController.navigate("community/myFavorites") },
                    onOpenPublished = { navController.navigate("community/myPublished") },
                    onOpenProfile = { navController.navigate("profile") },
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenLogin = { navController.navigate("login") }
                )
            }
            composable("profile") {
                ProfileRoute(container = container)
            }
            composable("settings") {
                SettingsRoute(container = container)
            }
            composable("login") {
                LoginRoute(container = container) {
                    navController.popBackStack()
                }
            }
        }
    }
}






