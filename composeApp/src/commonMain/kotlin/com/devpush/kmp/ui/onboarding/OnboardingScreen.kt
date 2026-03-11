package com.devpush.kmp.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kmp.composeapp.generated.resources.Res
import kmp.composeapp.generated.resources.onboarding_controller
import kmp.composeapp.generated.resources.onboarding_treasure
import kmp.composeapp.generated.resources.onboarding_trophy

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: DrawableResource
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Discover New Games",
        description = "Find the best games to play based on your preferences and ratings.",
        imageRes = Res.drawable.onboarding_controller
    ),
    OnboardingPage(
        title = "Track Your Progress",
        description = "Keep track of the games you've played and the treasures you've collected.",
        imageRes = Res.drawable.onboarding_treasure
    ),
    OnboardingPage(
        title = "Achieve Greatness",
        description = "Compete with others and earn trophies for your gaming achievements.",
        imageRes = Res.drawable.onboarding_trophy
    )
)

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onFinishOnboarding: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(page = onboardingPages[page])
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(onboardingPages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                val width = if (pagerState.currentPage == iteration) 24.dp else 12.dp
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .height(12.dp)
                        .width(width)
                )
            }
        }

        AnimatedVisibility(
            visible = pagerState.currentPage == onboardingPages.size - 1,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Button(
                onClick = onFinishOnboarding,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp)
            ) {
                Text("Get Started")
            }
        }
        
        if (pagerState.currentPage != onboardingPages.size - 1) {
            // Keep the space balanced when button is not shown
            Spacer(modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp).height(48.dp))
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(page.imageRes),
            contentDescription = page.title,
            modifier = Modifier.size(300.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
