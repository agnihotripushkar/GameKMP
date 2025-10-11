package com.devpush.features.gameDetails.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.devpush.features.gameDetails.domain.model.GameDetails
import kmp.features.generated.resources.Res
import kmp.features.generated.resources.developers
import kmp.features.generated.resources.game_count
import kmp.features.generated.resources.platforms
import kmp.features.generated.resources.stores
import kmp.features.generated.resources.tags
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GameDetailsScreen(modifier: Modifier = Modifier, id: String, onBackClick: () -> Unit) {

    val viewModel = koinViewModel<GameDetailsViewModel>()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(id) {
        viewModel.getGameDetails(id.toInt())
    }

    GameDetailsScreenContent(
        modifier = modifier.fillMaxSize(), uiState = uiState.value,
        onDelete = { viewModel.delete(it) },
        onSave = { id, name, image -> viewModel.save(id, image, name) },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameDetailsScreenContent(
    modifier: Modifier = Modifier,
    uiState: GameDetailsUiState,
    onDelete: (Int) -> Unit,
    onSave: (id: Int, title: String, image: String) -> Unit,
    onBackClick: () -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    if (uiState.error.isNotBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(uiState.error)
        }
    }


    uiState.data?.let { data ->
        Box(modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize())
            {

                item {
                    AsyncImage(
                        model = data.backgroundImage, contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(350.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        text = data.name,
                        style = MaterialTheme.typography.headlineSmall // M3: Was h4
                    )
                }

                item {
                    Text(
                        text = data.description, style = MaterialTheme.typography.bodyLarge, // M3: Was body1
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    )
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {

                        Text(
                            text = stringResource(Res.string.platforms),
                            style = MaterialTheme.typography.headlineSmall, // M3: Was h4
                            modifier = Modifier.padding(horizontal = 12.dp).padding(top = 24.dp)
                        )

                        LazyRow(modifier = Modifier.fillMaxWidth()) {
                            items(data.platforms) { 
                                Card(
                                    modifier = Modifier.padding(12.dp).wrapContentSize(),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp) // M3 elevation
                                ) {
                                    Column(
                                        modifier = Modifier.width(150.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        AsyncImage(
                                            model = it.image, contentDescription = null,
                                            modifier = Modifier
                                                .padding(top = 8.dp)
                                                .background(
                                                    color = Color.Transparent,
                                                    shape = CircleShape
                                                ).clip(CircleShape)
                                                .size(120.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                        Text(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            text = it.name,
                                            style = MaterialTheme.typography.bodySmall // M3: Was caption
                                        )

                                    }

                                }
                            }
                        }

                    }
                }



                item {
                    Text(
                        text = stringResource(Res.string.stores),
                        style = MaterialTheme.typography.headlineSmall, // M3: Was h4
                        modifier = Modifier.padding(horizontal = 12.dp).padding(top = 24.dp)
                            .padding(bottom = 12.dp)
                    )
                }

                items(data.stores) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp)
                            .padding(bottom = 8.dp).fillMaxWidth()
                    ) {

                        AsyncImage(
                            model = it.image, contentDescription = null,
                            modifier = Modifier.size(120.dp)
                                .background(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = it.name, style = MaterialTheme.typography.titleMedium, // M3: Was h6
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = it.domain, style = MaterialTheme.typography.bodyMedium, // M3: Was body2
                                textDecoration = TextDecoration.Underline
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(Res.string.game_count, it.gameCount),
                                style = MaterialTheme.typography.bodySmall // M3: Was caption
                            )

                        }

                    }
                }


                item {
                    Text(
                        text = stringResource(Res.string.tags),
                        style = MaterialTheme.typography.headlineSmall, // M3: Was h4
                        modifier = Modifier.padding(horizontal = 12.dp).padding(top = 24.dp)
                    )
                }

                item {
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
                    ) {

                        data.tags.forEach { 
                            Row(
                                modifier = Modifier.padding(top = 8.dp, end = 12.dp).background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(200.dp)
                                ).border(
                                    width = .5.dp,
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(200.dp)
                                )
                                    .clip(RoundedCornerShape(200.dp)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = it.image,
                                    contentDescription = null,
                                    modifier = Modifier.size(35.dp)
                                        .background(color = Color.Transparent, shape = CircleShape)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = it.name, style = MaterialTheme.typography.bodySmall, // M3: Was caption
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }

                    }
                }


                item {
                    Text(
                        text = stringResource(Res.string.developers),
                        style = MaterialTheme.typography.headlineSmall, // M3: Was h4
                        modifier = Modifier.padding(horizontal = 12.dp)
                            .padding(top = 24.dp, bottom = 12.dp)
                    )
                }

                items(data.developers) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {

                        AsyncImage(
                            model = it.image, contentDescription = null,
                            modifier = Modifier.size(120.dp)
                                .background(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = it.name, style = MaterialTheme.typography.titleMedium, // M3: Was h6
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(Res.string.game_count, it.gameCount),
                                style = MaterialTheme.typography.bodySmall // M3: Was caption
                            )
                        }
                    }

                }


            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            ) {

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(color = Color.White, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        onSave(data.id, data.name, data.backgroundImage)
                    },
                    modifier = Modifier.background(color = Color.White, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite, contentDescription = null,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))


                IconButton(
                    onClick = {
                        onDelete(data.id)
                    },
                    modifier = Modifier.background(color = Color.White, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete, contentDescription = null,
                        modifier = Modifier.padding(4.dp)
                    )
                }

            }

        }


    }


}

@Preview
@Composable
fun GameDetailsScreenContentPreview() {
    val sampleData = GameDetails(
        id = 1,
        name = "Sample Game",
        description = "This is a sample game description.",
        backgroundImage = "",
        platforms = listOf(),
        stores = listOf(),
        tags = listOf(),
        developers = listOf(),
        additionalImage = null
    )
    GameDetailsScreenContent(
        uiState = GameDetailsUiState(data = sampleData),
        onDelete = {},
        onSave = { _, _, _ -> },
        onBackClick = {}
    )
}