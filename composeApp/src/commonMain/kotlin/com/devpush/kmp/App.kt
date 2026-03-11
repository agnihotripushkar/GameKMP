package com.devpush.kmp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.devpush.kmp.navigation.GameNavGraph
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import kmp.composeapp.generated.resources.Res
import kmp.composeapp.generated.resources.compose_multiplatform

import androidx.navigation.createGraph
import io.github.aakira.napier.Napier

@Composable
@Preview
fun App() {
    Napier.v("Hello Napier")
    MaterialTheme {
        val navHostController = rememberNavController()
        val bottomPadding = WindowInsets.statusBars.asPaddingValues().calculateBottomPadding()

        val navGraph = remember(navHostController) {
            navHostController.createGraph(startDestination = GameNavGraph.Dest.Root) {
                GameNavGraph.build(
                    modifier = Modifier.padding(top = bottomPadding).fillMaxSize(),
                    navHostController = navHostController,
                    navGraphBuilder = this
                )
            }
        }

        NavHost(
            navController = navHostController,
            graph = navGraph,
            modifier = Modifier.padding(top = bottomPadding).fillMaxSize(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 300 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -300 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -300 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 300 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        )
    }
}