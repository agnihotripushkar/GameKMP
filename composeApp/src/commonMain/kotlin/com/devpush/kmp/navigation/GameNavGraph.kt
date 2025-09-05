package com.devpush.kmp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpush.features.game.ui.GameScreen

object GameNavGraph: BaseNavGraph {

    sealed class Dest(val route: String) {

        data object Root : Dest("/game-root")

        data object Game : Dest("/game")

        data object Details : Dest("/game_details/{id}") {
            fun getRoute(id: Int) = "/game_details/${id}"
        }

    }

    override fun build(
        modifier: Modifier,
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder
    ) {
        navGraphBuilder.navigation(route = Dest.Root.route, startDestination = Dest.Game.route) {
            composable(route = Dest.Game.route) {
                GameScreen(
                    modifier = modifier.fillMaxSize(),
                     onClick = {
                        navHostController.navigate(Dest.Details.getRoute(it))
                    })
            }

        }
    }
}