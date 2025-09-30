package com.h3110w0r1d.t9launcher.ui.screen

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig
import com.h3110w0r1d.t9launcher.model.LocalGlobalViewModel
import com.h3110w0r1d.t9launcher.ui.LocalNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen() {
    val navController = LocalNavController.current
    val viewModel = LocalGlobalViewModel.current
    val longClickMenuVector = AnimatedImageVector.animatedVectorResource(R.drawable.long_click_menu)
    val longClickMenuText = stringResource(R.string.long_click_menu)
    val longClickDeleteVector = AnimatedImageVector.animatedVectorResource(R.drawable.long_click_delete)
    val longClickDeleteText = stringResource(R.string.long_click_delete)
    val longClickShowHideVector = AnimatedImageVector.animatedVectorResource(R.drawable.long_click_show_hide)
    val longClickShowHideText = stringResource(R.string.long_click_show_hide)
    val slideDeleteVector = AnimatedImageVector.animatedVectorResource(R.drawable.slide_delete)
    val slideDeleteText = stringResource(R.string.slide_delete)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val appConfig = LocalAppConfig.current

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize(),
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
            ) { page ->
                when (page) {
                    0 -> OnboardingPager(longClickDeleteVector, longClickDeleteText)
                    1 -> OnboardingPager(longClickShowHideVector, longClickShowHideText)
                    2 -> OnboardingPager(longClickMenuVector, longClickMenuText)
                    3 -> OnboardingPager(slideDeleteVector, slideDeleteText)
                }
            }
            Button(
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                enabled = pagerState.currentPage > 0,
            ) {
                Text(stringResource(R.string.back))
            }

            Button(
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                onClick = {
                    if (pagerState.currentPage == pagerState.pageCount - 1) {
                        if (!appConfig.isShowedOnboarding) {
                            viewModel.setShowedOnboarding()
                        } else {
                            navController?.popBackStack()
                        }
                        return@Button
                    }
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
            ) {
                if (pagerState.currentPage < pagerState.pageCount - 1) {
                    Text(stringResource(R.string.next))
                } else {
                    Text(stringResource(R.string.get_started))
                }
            }
        }
    }
}

@Composable
fun OnboardingPager(
    image: AnimatedImageVector,
    text: String,
) {
    var playIndex by remember { mutableIntStateOf(1) }
    var isAtEnd by remember { mutableStateOf(false) }

    LaunchedEffect(playIndex) {
        // 等待完整播放时长
        delay(image.totalDuration.toLong() + 1000)
        // 重置索引，触发重新创建 painter
        isAtEnd = false
        playIndex = -playIndex
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        key(playIndex) {
            val painter = rememberAnimatedVectorPainter(image, atEnd = isAtEnd)
            Image(
                modifier = Modifier.fillMaxWidth(.7f).fillMaxHeight(.8f).padding(bottom = 120.dp),
                painter = painter,
                contentDescription = null,
            )
            isAtEnd = true
            Text(text, modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp, 120.dp))
        }
    }
}
